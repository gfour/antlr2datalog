package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.clyze.persistent.metadata.Configuration;
import org.clyze.persistent.metadata.Printer;
import org.clyze.persistent.metadata.SourceFileReporter;
import org.clyze.persistent.metadata.SourceMetadata;
import org.clyze.persistent.model.Function;
import org.clyze.persistent.model.Position;
import org.clyze.persistent.model.Type;

/**
 * This class generates source code metadata for UI integration of the analysis results.
 */
public class MetadataGenerator {
    /** The name of the output file. */
    public static String OUTPUT_FILE = "metadata.json";

    private final File outputDabase;
    public MetadataGenerator(File outputDatabase) {
        this.outputDabase = outputDatabase;
    }

    public void run() {
        System.out.println("Generating metadata...");

        SourceMetadata metadata = new SourceMetadata();

        process("OO_Class.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            String[] loc = parts[2].split(":");
            long startLine = Long.parseLong(loc[1]);
            long startCol = Long.parseLong(loc[2]);
            Position pos = new Position(startLine, startLine, startCol, startCol + 1);
            String sourceFileName = loc[0];
            metadata.types.add(new Type(pos, sourceFileName, id, name));
        }));

        process("BASE_FunctionDefinition.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            String[] loc = parts[2].split(":");
            long startLine = Long.parseLong(loc[1]);
            long startCol = Long.parseLong(loc[2]);
            Position pos = new Position(startLine, startLine, startCol, startCol + name.length());
            String sourceFileName = loc[0];
            // TODO: fill in params
            String[] params = new String[] { };
            // TODO: fix outer position
            metadata.functions.add(new Function(pos, sourceFileName, id, name, params, pos));
        }));

        try {
            SourceFileReporter fileReporter = new SourceFileReporter(new Configuration(new Printer(true)), metadata);
            fileReporter.createReportFile(new File(outputDabase, OUTPUT_FILE).getCanonicalPath());
            fileReporter.printReportStats();
        } catch (IOException ex) {
            System.err.println("ERROR: failed to generate metadata: " + ex.getMessage());
        }
    }

    void process(String relationFile, Consumer<String[]> proc) {
        File rel = new File(outputDabase, relationFile);
        if (!rel.exists()) {
            System.out.println("File does not exist: " + relationFile);
            return;
        }
        try (Stream<String> lines = Files.lines(rel.toPath())) {
            lines.forEach((String line) -> {
                try {
                    String[] parts = line.split("\t");
                    proc.accept(parts);
                } catch (Exception ex) {
                    System.out.println("ERROR: failed to prcess line: " + line);
                    ex.printStackTrace();
                }
            });
        } catch (IOException ex) {
            System.err.println("ERROR: failed to parse metadata relation " + relationFile + ": " + ex.getMessage());
        }
    }
}
