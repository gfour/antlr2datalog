package org.clyze.antlr2datalog;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main driver that guides schema detection and source code parsing.
 */
public class Driver {
    private final ParserConfiguration parserConfiguration;

    /**
     * Create a new driver to generate the schema and parse the sources.
     * @param parserConfiguration    the parser configuration to use
     */
    public Driver(ParserConfiguration parserConfiguration) {
        this.parserConfiguration = parserConfiguration;
    }

    /**
     * Main entry point, generates the logic schema and then populates the
     * facts database.
     * @param schemaPath   the file path to use to write the schema (example: "schema.dl")
     * @param factsDir     the directory path to use for writing the facts
     * @param inputs       the inputs (source files or directories)
     */
    public void generateSchemaAndParseSources(String schemaPath, String factsDir,
                                              String[] inputs) {
        System.out.println("Discovering schema...");
        SchemaFinder sf = new SchemaFinder(parserConfiguration);
        Map<Class<?>, Collection<Component>> schema = sf.computeSchema();
        sf.printSchema(new File(schemaPath));

        System.out.println("Recording facts...");
        Map<String, Collection<String>> tables = new HashMap<>();
        Database db = new Database(tables, factsDir);
        AtomicInteger counter = new AtomicInteger(0);
        for (String path : inputs)
            parseFile(schema, db, counter, path);
        db.writeFacts();
    }

    private void parseFile(Map<Class<?>, Collection<Component>> schema, Database db, AtomicInteger counter, String path) {
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            if (Main.debug)
                System.out.println("Processing directory: " + path);
            File[] files = pathFile.listFiles();
            if (files != null)
                for (File f : files)
                    try {
                        parseFile(schema, db, counter, f.getCanonicalPath());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
            return;
        }

        boolean ignore = true;
        for (String ext : parserConfiguration.extensions)
            if (path.endsWith(ext)) {
                ignore = false;
                break;
            }
        if (ignore) {
            if (Main.debug)
                System.out.println("Ignoring: " + path);
            return;
        }
        try (InputStream inputStream = new FileInputStream(path)) {
            Lexer lexer = parserConfiguration.lexerClass.getConstructor(CharStream.class).newInstance(CharStreams.fromStream(inputStream));
            TokenStream tokenStream = new CommonTokenStream(lexer);
            Parser parser = parserConfiguration.parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
            ParserRuleContext ruleContext = (ParserRuleContext) parserConfiguration.rootNodeMethod.invoke(parser);
            process(db, path, schema, counter, ruleContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void process(Database db, String path, Map<Class<?>, Collection<Component>> schema, AtomicInteger counter, ParserRuleContext rootNode) {
        int fileId = counter.getAndIncrement();
        FactVisitor fv = new FactVisitor(fileId, schema, db);
        rootNode.accept(new ParseTreeVisitor<Void>() {
            @Override public Void visit(ParseTree parseTree) {
                String parseTreeRelationName = parseTree.getClass().getSimpleName();
                db.writeRow(BaseSchema.SOURCE_FILE_ID, path + '\t' + fileId + '\t' + fv.getNodeId(parseTreeRelationName, parseTree));
                fv.visitParseTree(new TypedParseTree(parseTree, parseTree.getClass()));
                return null;
            }
            @Override public Void visitChildren(RuleNode ruleNode) { return visit(ruleNode); }
            @Override public Void visitTerminal(TerminalNode terminalNode) { return null; }
            @Override public Void visitErrorNode(ErrorNode errorNode) { return null; }
        });
    }
}
