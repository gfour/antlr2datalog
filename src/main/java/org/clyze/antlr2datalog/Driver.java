package org.clyze.antlr2datalog;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
 * The main driver that guides schema detection and source code parsing.
 */
public class Driver {
    private final List<ParserConfiguration> parserConfigurations;
    private final File workspaceDir;
    private final boolean debug;

    /**
     * Create a new driver to generate the schema and parse the sources.
     * @param parserConfigurations   the parser configurations to use
     * @param workspaceDir           the workspace directory to use
     * @param debug                  debug mode
     */
    public Driver(List<ParserConfiguration> parserConfigurations, File workspaceDir, boolean debug) {
        this.parserConfigurations = parserConfigurations;
        this.workspaceDir = workspaceDir;
        this.debug = debug;
    }

    /**
     * Returns the generated schema path.
     * @return  the file path to use to write the schema (example: "schema.dl")
     */
    private File getSchemaFile() {
       return new File(workspaceDir, "schema.dl");
    }

    /**
     * Returns the facts directory.
     * @return  the directory path to use for writing the facts
     */
    private File getFactsDir() {
        return new File(workspaceDir, "facts");
    }

    /**
     * Initializes the workspace directory.
     */
    public void initWorkspaceDir() {
        if (workspaceDir.exists() || !workspaceDir.mkdirs())
            System.out.println("WARNING: workspace directory already exists: " + workspaceDir);
    }

    /**
     * Main entry point, generates the logic schema and then populates the
     * facts database.
     * @param inputs       the inputs (source files or directories)
     * @param topPath      if not null, make paths relative to this path
     * @throws IOException on schema writing failure
     */
    public void generateSchemaAndParseSources(String[] inputs, String topPath)
    throws IOException {
        List<Schema> langSchemas = new ArrayList<>();
        Database baseDb = new Database(BaseSchema.create(), getFactsDir());
        for (ParserConfiguration parserConfiguration : parserConfigurations) {
            System.out.println("Discovering " + parserConfiguration.name + " schema...");
            SchemaFinder sf = new SchemaFinder(parserConfiguration);
            Schema langSchema = sf.generateLanguageSchema();
            langSchemas.add(langSchema);

            System.out.println("Recording " + parserConfiguration.name + " facts...");
            Database db = new Database(langSchema, getFactsDir());
            AtomicInteger counter = new AtomicInteger(0);
            for (String path : inputs)
                parseFile(parserConfiguration, db, baseDb, counter, path, topPath);
            db.writeFacts(debug);
        }
        baseDb.writeFacts(debug);
        Schema.write(baseDb.schema, langSchemas, getSchemaFile());
    }

    private void parseFile(ParserConfiguration parserConfiguration,
                           Database langDb, Database baseDb,
                           AtomicInteger counter, String path, String topPath) {
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            if (debug)
                System.out.println("[" + parserConfiguration.name + "] Processing directory: " + path);
            File[] files = pathFile.listFiles();
            if (files != null)
                for (File f : files)
                    try {
                        parseFile(parserConfiguration, langDb, baseDb, counter, f.getCanonicalPath(), topPath);
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
            if (debug)
                System.out.println("[" + parserConfiguration.name + "] Ignoring: " + path);
            return;
        }
        try (InputStream inputStream = new FileInputStream(path)) {
            CharStream cs = parserConfiguration.getCharStream(path, inputStream);
            Lexer lexer = parserConfiguration.lexerClass.getConstructor(CharStream.class).newInstance(cs);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            Parser parser = parserConfiguration.parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
            if (debug) {
                parser.addParseListener(new ParseTreeListener() {
                    @Override
                    public void visitErrorNode(ErrorNode errorNode) {
                        System.out.println("ERROR: in node: '" + errorNode.getText() + "'");
                    }

                    @Override
                    public void visitTerminal(TerminalNode terminalNode) {
                    }

                    @Override
                    public void enterEveryRule(ParserRuleContext parserRuleContext) {
                    }

                    @Override
                    public void exitEveryRule(ParserRuleContext parserRuleContext) {
                    }
                });
            }
            ParserRuleContext ruleContext = (ParserRuleContext) parserConfiguration.rootNodeMethod.invoke(parser);
            process(langDb, baseDb, path, counter, ruleContext, topPath);
        } catch (UnsupportedParserException ignored) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void process(Database langDb, Database baseDb, String path,
                         AtomicInteger counter, ParserRuleContext rootNode, String topPath) {
        int fileId = counter.getAndIncrement();
        FactVisitor fv = new FactVisitor(fileId, langDb, baseDb);
        rootNode.accept(new ParseTreeVisitor<Void>() {
            @Override public Void visit(ParseTree parseTree) {
                String parseTreeRelationName = SchemaFinder.getSimpleName(parseTree.getClass(), langDb.schema.rules);
                String srcPath = getRelativePath(path, topPath);
                BaseSchema.writeSourceFileId(baseDb, srcPath + '\t' + fileId + '\t' + fv.getNodeId(parseTreeRelationName, parseTree));
                fv.visitParseTree(new TypedParseTree(parseTree, parseTree.getClass()));
                return null;
            }
            @Override public Void visitChildren(RuleNode ruleNode) { return visit(ruleNode); }
            @Override public Void visitTerminal(TerminalNode terminalNode) { return null; }
            @Override public Void visitErrorNode(ErrorNode errorNode) { return null; }
        });
    }

    public static String getRelativePath(String path, String topPath) {
        if (topPath == null || !path.startsWith(topPath))
            return path;
        String r = path.substring(topPath.length());
        String sep = File.separator;
        return (r.length() > 0 && r.startsWith(sep)) ? r.substring(sep.length()) : r;
    }

    private static void createDir(File dir) {
        if (!dir.exists())
            if (!dir.mkdirs())
                System.out.println("WARNING: directory already exists: " + dir);
    }

    /**
     * Returns the directory contain the Datalog analysis logic. This works
     * for both local logic (running inside in the repo) and for bundled logic.
     * @return              the directory object
     * @throws IOException  on logic I/O error
     */
    private File getLogicDir() throws IOException {
        final String LOGIC_DIR_NAME = "logic";
        File logicDir = new File(LOGIC_DIR_NAME);
        if (logicDir.exists())
            return logicDir;
        else
            return Resources.extractResourceArchive(getClass().getClassLoader(), LOGIC_DIR_NAME, "logic.zip");
    }

    /**
     * Run the analysis logic.
     * @param compile                if true, logic is compiled to a binary
     * @throws IOException           on file handling error
     * @throws InterruptedException  on command execution error
     */
    public void runLogic(boolean compile)
            throws IOException, InterruptedException {
        File logicDir = getLogicDir();
        System.out.println("Using logic directory: " + logicDir);
        File logicIn = new File(workspaceDir, "logic-pre.dl");
        try (FileWriter fw = new FileWriter(logicIn)) {
            for (ParserConfiguration parserConfiguration : parserConfigurations) {
                String language = parserConfiguration.name().toLowerCase(Locale.ROOT);
                String logicName = language + "-logic.dl";
                File logic = new File(logicDir, logicName);
                if (!logic.exists())
                    throw new RuntimeException("ERROR: no logic (" + logic.getCanonicalPath() + ") available for language: " + language);
                fw.write("#include \"" + logicName + "\"\n");
            }
        }
        String logicOut = (new File(workspaceDir, "logic-out.dl")).getCanonicalPath();
        List<String> args = new LinkedList<>(Arrays.asList("cpp", "-I" + logicDir.getCanonicalPath(), "-I" + workspaceDir.getCanonicalPath(), "-P", logicIn.getCanonicalPath(), "-o", logicOut));
        if (debug)
            args.add("-DDEBUG");
        ProcessBuilder cpp = new ProcessBuilder(args.toArray(new String[0]));
        runWithOutput(cpp);
        File outputDatabase = new File(workspaceDir, "database");
        createDir(outputDatabase);
        String outDatabasePath = outputDatabase.getCanonicalPath();
        if (compile) {
            System.out.println("Compiling logic...");
            final String ANALYZER_NAME = "analyzer";
            ProcessBuilder souffle = new ProcessBuilder("souffle", "-c", logicOut, "-o", ANALYZER_NAME);
            souffle.directory(workspaceDir);
            souffle.redirectErrorStream(true);
            runWithOutput(souffle);
        }

        System.out.println("Running logic...");
        List<String> cmd = new LinkedList<>();
        if (compile) {
            String time = "/usr/bin/time";
            if ((new File(time)).exists())
                cmd.add(time);
            cmd.add("./analyzer");
        } else {
            cmd.add("souffle");
            cmd.add(logicOut);
        }
        cmd.addAll(Arrays.asList("-F", getFactsDir().getCanonicalPath(), "-D", outDatabasePath));
        String[] cmdLine = cmd.toArray(new String[0]);
        System.out.println("Running: " + String.join(" ", cmdLine));
        ProcessBuilder souffle = new ProcessBuilder(cmdLine);
        souffle.directory(workspaceDir);
        runWithOutput(souffle);
        System.out.println("Results written to: " + outDatabasePath);
    }

    private static void runWithOutput(ProcessBuilder pb) throws InterruptedException, IOException {
        Process proc = pb.start();
        proc.waitFor();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        stdInput.lines().forEach(System.out::println);
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        stdError.lines().forEach(System.out::println);
    }
}

class UnsupportedParserException extends Exception {}
