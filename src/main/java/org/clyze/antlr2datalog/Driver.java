package org.clyze.antlr2datalog;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
 * The main driver that guides schema detection and source code parsing.
 */
public class Driver {
    private final ParserConfiguration parserConfiguration;
    private final File workspaceDir;

    /**
     * Create a new driver to generate the schema and parse the sources.
     * @param parserConfiguration    the parser configuration to use
     * @param workspaceDir           the workspace directory to use
     */
    public Driver(ParserConfiguration parserConfiguration, File workspaceDir) {
        this.parserConfiguration = parserConfiguration;
        this.workspaceDir = workspaceDir;
    }

    /**
     * Returns the generated schema path.
     * @return  the file path to use to write the schema (example: "schema.dl")
     */
    private File getSchemaFile() {
       return new File(getWorkspaceDir(), "schema.dl");
    }

    /**
     * Returns the facts directory.
     * @return  the directory path to use for writing the facts
     */
    private File getFactsDir() {
        return new File(getWorkspaceDir(), "facts");
    }

    /**
     * Returns the workspace directory.
     * @return the workspace directory to use
     */
    private File getWorkspaceDir() {
        if (!workspaceDir.exists()) {
            if (!workspaceDir.mkdirs())
                System.out.println("WARNING: workspace directory already exists: " + workspaceDir);
        }
        return workspaceDir;
    }
    /**
     * Main entry point, generates the logic schema and then populates the
     * facts database.
     * @param inputs       the inputs (source files or directories)
     * @param topPath      if not null, make paths relative to this path
     */
    public void generateSchemaAndParseSources(String[] inputs, String topPath) {
        System.out.println("Discovering schema...");
        SchemaFinder sf = new SchemaFinder(parserConfiguration);
        Map<Class<?>, Rule> schema = sf.computeSchema();
        List<String> relations = sf.generateSchema(getSchemaFile());

        System.out.println("Recording facts...");
        Database db = new Database(relations, getFactsDir());
        AtomicInteger counter = new AtomicInteger(0);
        for (String path : inputs)
            parseFile(schema, db, counter, path, topPath);
        db.writeFacts();
    }

    private void parseFile(Map<Class<?>, Rule> schema, Database db,
                           AtomicInteger counter, String path, String topPath) {
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            if (Main.debug)
                System.out.println("Processing directory: " + path);
            File[] files = pathFile.listFiles();
            if (files != null)
                for (File f : files)
                    try {
                        parseFile(schema, db, counter, f.getCanonicalPath(), topPath);
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
            CharStream cs = getCharStream(path, inputStream);
            Lexer lexer = parserConfiguration.lexerClass.getConstructor(CharStream.class).newInstance(cs);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            Parser parser = parserConfiguration.parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
            if (Main.debug) {
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
            process(db, path, schema, counter, ruleContext, topPath);
        } catch (UnsupportedParserException ignored) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CharStream getCharStream(String path, InputStream inputStream) throws IOException, UnsupportedParserException {
        if (parserConfiguration.lowerCase) {
            // The required class is not compatible with Java 8. We use reflection below
            // to break compile dependency.
            try {
                Class<? extends CharStream> c = (Class<? extends CharStream>) Class.forName("com.khubla.antlr.antlr4test.filestream.AntlrCaseInsensitiveFileStream");
                Class<?> ciType = Class.forName("com.khubla.antlr.antlr4test.CaseInsensitiveType");
                Constructor<? extends CharStream> constr = c.getConstructor(String.class, String.class, ciType);
                return constr.newInstance(path, "UTF-8", ciType.cast(ciType.getDeclaredField("lower").get(null)));
            } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("This parser seems to require lowercase char streams. Uncomment dependency 'antlr4test-maven-plugin'");
                System.out.println("and rebuild. Enabling this feature may not support Java 8.");
                throw new UnsupportedParserException();
            }
        } else
            return CharStreams.fromStream(inputStream);
    }

    private void process(Database db, String path, Map<Class<?>, Rule> schema,
                         AtomicInteger counter, ParserRuleContext rootNode, String topPath) {
        int fileId = counter.getAndIncrement();
        FactVisitor fv = new FactVisitor(fileId, schema, db);
        rootNode.accept(new ParseTreeVisitor<Void>() {
            @Override public Void visit(ParseTree parseTree) {
                String parseTreeRelationName = SchemaFinder.getSimpleName(parseTree.getClass(), schema);
                String srcPath = getRelativePath(path, topPath);
                db.writeRow(BaseSchema.SOURCE_FILE_ID, srcPath + '\t' + fileId + '\t' + fv.getNodeId(parseTreeRelationName, parseTree));
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
     * @param debug         if true, print diagnistic messages
     * @return              the directory object
     * @throws IOException  on logic I/O error
     */
    private File getLogicDir(boolean debug) throws IOException {
        final String LOGIC_DIR_NAME = "logic";
        File logicDir = new File(LOGIC_DIR_NAME);
        if (logicDir.exists())
            return logicDir;
        else
            return Resources.extractResourceArchive(getClass().getClassLoader(), LOGIC_DIR_NAME, "logic.zip", debug);
    }

    /**
     * Run the analysis logic.
     * @param compile                if true, logic is compiled to a binary
     * @param debug                  if true, enable debugging logic
     * @throws IOException           on file handling error
     * @throws InterruptedException  on command execution error
     */
    public void runLogic(boolean compile, boolean debug)
            throws IOException, InterruptedException {
        File workspaceDir = getWorkspaceDir();
        String language = parserConfiguration.name().toLowerCase(Locale.ROOT);
        File logicDir = getLogicDir(debug);
        System.out.println("Using logic: " + logicDir);
        File logic = new File(logicDir, language + "-logic.dl");
        if (!logic.exists())
            throw new RuntimeException("ERROR: no logic (" + logic.getCanonicalPath() + ") available for language: " + language);
        String logicOut = (new File(workspaceDir, "logic-out.dl")).getCanonicalPath();
        List<String> args = new LinkedList<>(Arrays.asList("cpp", "-I" + logicDir.getCanonicalPath(), "-I" + workspaceDir.getCanonicalPath(), "-P", logic.getCanonicalPath(), "-o", logicOut));
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