package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;
import org.clyze.persistent.metadata.Configuration;
import org.clyze.persistent.metadata.Printer;
import org.clyze.persistent.metadata.SourceFileReporter;
import org.clyze.persistent.metadata.SourceMetadata;
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
        String ooClassDb = "OO_Class.csv";
        try (Stream<String> lines = Files.lines((new File(outputDabase, ooClassDb)).toPath())) {
            lines.forEach((String line) -> {
                try {
                    String[] parts = line.split("\t");
                    String id = parts[0];
                    String name = parts[1];
                    String[] loc = parts[2].split(":");
                    long startLine = Long.parseLong(loc[1]);
                    long startCol = Long.parseLong(loc[2]);
                    Position pos = new Position(startLine, startLine, startCol, startCol + 1);
                    String sourceFileName = loc[0];
                    Type type = new Type(pos, sourceFileName, id, name);
                    metadata.types.add(type);
                } catch (Exception ex) {
                    System.out.println("ERROR: failed to prcess line: " + line);
                    ex.printStackTrace();
                }
            });
        } catch (IOException ex) {
            System.err.println("ERROR: failed to parse metadata relations: " + ex.getMessage());
        }

        try {
            SourceFileReporter fileReporter = new SourceFileReporter(new Configuration(new Printer(true)), metadata);
            fileReporter.createReportFile(new File(outputDabase, OUTPUT_FILE).getCanonicalPath());
            if (Main.debug)
                fileReporter.printReportStats();
        } catch (IOException ex) {
            System.err.println("ERROR: failed to generate metadata: " + ex.getMessage());
        }
    }
}
