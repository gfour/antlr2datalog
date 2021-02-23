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
    public static final String OUTPUT_FILE = "metadata.json";

    private final File outputDabase;
    public MetadataGenerator(File outputDatabase) {
        this.outputDabase = outputDatabase;
    }

    public void run() {
        System.out.println("Generating metadata...");

        SourceMetadata metadata = new SourceMetadata();

        process("BASE_Type.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            SourcePosition srcPos = getSourcePosition(parts[2], name.length());
            metadata.types.add(new Type(srcPos.position, srcPos.sourceFileName, id, name));
        }));

        process("BASE_FunctionDefinition.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            SourcePosition srcPos = getSourcePosition(parts[2], name.length());
            // TODO: fill in params
            String[] params = new String[] { };
            // TODO: fix outer position
            metadata.functions.add(new Function(srcPos.position, srcPos.sourceFileName, id, name, params, srcPos.position));
        }));

        try {
            SourceFileReporter fileReporter = new SourceFileReporter(new Configuration(new Printer(true)), metadata);
            fileReporter.createReportFile(new File(outputDabase, OUTPUT_FILE).getCanonicalPath());
            fileReporter.printReportStats();
        } catch (IOException ex) {
            System.err.println("ERROR: failed to generate metadata: " + ex.getMessage());
        }
    }

    private static SourcePosition getSourcePosition(String location, int length) {
        String[] loc = location.split(":");
        if (loc.length != 3) {
            System.err.println("ERROR: malformed location: " + location);
            return null;
        }
        String sourceFileName = loc[0];
        long startLine = Long.parseLong(loc[1]);
        // Use 1-based column numbering.
        long startCol = Long.parseLong(loc[2]) + 1;
        Position position = new Position(startLine, startLine, startCol, startCol + length);
        return new SourcePosition(sourceFileName, position);
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

class SourcePosition {
    final String sourceFileName;
    final Position position;

    SourcePosition(String sourceFileName, Position position) {
        this.sourceFileName = sourceFileName;
        this.position = position;
    }
}