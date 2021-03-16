package org.clyze.antlr2datalog;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import org.apache.commons.cli.*;

public class Main {
    /** Global debug flag. */
    public static boolean debug = false;
    /** The name of the default workspace directory. */
    public static final String DEFAULT_WORKSPACE = "workspace";

    /**
     * The main entry point.
     * @param args  the command-line arguments
     */
    public static void main(String[] args) {
        Options options = new Options();

        Option helpOpt = new Option("h", "help", false, "Show this help text.");
        options.addOption(helpOpt);

        Option inputOpt = new Option("i", "input", true, "File or directory containing source files.");
        inputOpt.setRequired(true);
        inputOpt.setArgName("PATH");
        inputOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(inputOpt);

        Option langOpt = new Option("l", "language", true, "Parser language (available values: " + Arrays.toString(ParserConfiguration.valuesLowercase()) + ").");
        langOpt.setRequired(true);
        langOpt.setArgName("LANGUAGE");
        inputOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(langOpt);

        Option workspaceOpt = new Option("w", "workspace", true, "Workspace directory (default: " + DEFAULT_WORKSPACE + ").");
        workspaceOpt.setArgName("PATH");
        options.addOption(workspaceOpt);

        Option debugOpt = new Option("d", "debug", false, "Enable debug mode.");
        options.addOption(debugOpt);

        Option compileOpt = new Option("c", "compile", false, "Compile logic.");
        options.addOption(compileOpt);

        Option relPathOpt = new Option(null, "relative-path", true, "Make source file paths in element locations relative to given path.");
        relPathOpt.setArgName("PATH");
        options.addOption(relPathOpt);

        Option genMetadataOpt = new Option("g", "generate-metadata", false, "Generate source code metadata.");
        options.addOption(genMetadataOpt);

        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            printUsage(options);
            return;
        }

        List<ParserReflection> parserConfigurations = new ArrayList<>();
        String workspaceDir = DEFAULT_WORKSPACE;
        boolean compile, generateMetadata;
        String relativePath;
        String[] inputs;
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cli = parser.parse(options, args);
            debug = cli.hasOption(debugOpt.getOpt());
            compile = cli.hasOption(compileOpt.getOpt());
            generateMetadata = cli.hasOption(genMetadataOpt.getOpt());
            String[] langs = cli.getOptionValues(langOpt.getOpt());
            inputs = cli.getOptionValues(inputOpt.getOpt());
            relativePath = cli.getOptionValue(relPathOpt.getLongOpt());
            if (relativePath != null && inputs.length > 1)
                System.out.println("Making all paths relative to " + relativePath + ", ensure that no duplicate paths appear in the sources.");
            if (cli.hasOption(workspaceOpt.getOpt()))
                workspaceDir = cli.getOptionValue(workspaceOpt.getOpt());
            System.out.println("Using workspace directory: " + workspaceDir);
            for (String lang : langs) {
                ParserConfiguration pc = ParserConfiguration.valueOf(lang.toUpperCase());
                parserConfigurations.add(new ParserReflection(pc, debug));
                System.out.println("Using language: " + pc.name);
            }
        } catch (ParseException | MalformedURLException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
            printUsage(options);
            return;
        }

        try {
            Driver driver = new Driver(parserConfigurations, new File(workspaceDir), debug);
            driver.initWorkspaceDir();
            driver.generateSchemaAndParseSources(inputs, relativePath);
            driver.runLogic(compile);
            if (generateMetadata)
                (new MetadataGenerator(driver.getOutputDatabase())).run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.printHelp("antlr2datalog [OPTION]...", options);
    }
}
