package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.clyze.persistent.metadata.*;
import org.clyze.persistent.model.*;

/**
 * This class generates source code metadata for UI integration of the analysis results.
 */
public class MetadataGenerator {
    /** The name of the output file. */
    public static final String OUTPUT_FILE = "metadata.json";

    private final File outputDabase;
    private final Map<String, String> artifactMap = new HashMap<>();

    public MetadataGenerator(File outputDatabase) {
        this.outputDabase = outputDatabase;
    }

    public void run() {
        System.out.println("Generating metadata...");

        SourceMetadata metadata = new SourceMetadata();

        process("BASE_SourceFileId.csv", (String[] parts) -> {
            String artifact = parts[0];
            String path = parts[1];
            String id = parts[3];
            metadata.sourceFiles.add(new SourceFile(artifact, path, id));
            artifactMap.put(path, artifact);
        });

        process("BASE_Type.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            SourcePosition srcPos = getSourcePosition(parts[2], name.length());
            if (srcPos == null) {
                System.err.println("WARNING: no source position for parts = " + Arrays.toString(parts));
                return;
            }
            metadata.types.add(new Type(srcPos.position, srcPos.sourceFileName, true, srcPos.artifact, id, name));
        }));

        Map<String, Position> functionAreas = new HashMap<>();
        process("BASE_Function_Area.csv", ((String[] parts) -> {
            String id = parts[0];
            long startLine = Long.parseLong(parts[1]);
            long startColumn = Long.parseLong(parts[2]) + 1;
            long endLine = Long.parseLong(parts[3]);
            long endColumn = Long.parseLong(parts[4]) + 1;
            Position area = new Position(startLine, endLine, startColumn, endColumn);
            functionAreas.put(id, area);
        }));

        Map<String, TreeMap<String, String>> functionParams = new HashMap<>();
        process("BASE_FunctionParameter.csv", ((String[] parts) -> {
            String fd = parts[0];
            String v = parts[2];
            String idx = parts[3];
            // Maintain a map that is sorted by the parameter index.
            TreeMap<String, String> params = functionParams.computeIfAbsent(fd, k -> new TreeMap<>());
            params.put(idx, v);
        }));

        process("BASE_FunctionDefinition.csv", ((String[] parts) -> {
            String fd = parts[0];
            String name = parts[1];
            SourcePosition srcPos = getSourcePosition(parts[2], name.length());
            if (srcPos == null) {
                System.err.println("WARNING: no source position for parts = " + Arrays.toString(parts));
                return;
            }
            TreeMap<String, String> fParams = functionParams.get(fd);
            String[] params = fParams == null ? new String[] { } : fParams.values().toArray(new String[0]);
            Position area = functionAreas.get(fd);
            if (area == null) {
                area = srcPos.position;
                if (Main.debug)
                    System.err.println("WARNING: function " + fd + " has no area information.");
            }
            metadata.functions.add(new Function(srcPos.position, srcPos.sourceFileName, true, srcPos.artifact, fd, name, params, area));
        }));

        process("BASE_VariableDeclaration.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            String loc = parts[2];
            SourcePosition srcPos = getSourcePosition(loc, name.length());
            if (srcPos == null) {
                System.err.println("WARNING: no source position for parts = " + Arrays.toString(parts));
                return;
            }
            metadata.variables.add(new Variable(srcPos.position, srcPos.sourceFileName, true, srcPos.artifact, id, name, true, false));
        }));

        process("BASE_Field.csv", ((String[] parts) -> {
            String id = parts[0];
            String name = parts[1];
            String loc = parts[2];
            SourcePosition srcPos = getSourcePosition(loc, name.length());
            if (srcPos == null) {
                System.err.println("WARNING: no source position for parts = " + Arrays.toString(parts));
                return;
            }
            metadata.fields.add(new Field(srcPos.position, srcPos.sourceFileName, true, srcPos.artifact, name, id));
        }));

        try {
            FileReporter fileReporter = new FileReporter(new Configuration(new Printer(true)), metadata);
            fileReporter.createReportFile(new File(outputDabase, OUTPUT_FILE).getCanonicalPath());
            fileReporter.printReportStats();
        } catch (IOException ex) {
            System.err.println("ERROR: failed to generate metadata: " + ex.getMessage());
        }
    }

    private SourcePosition getSourcePosition(String location, int length) {
        String[] loc = location.split(":");
        if (loc.length != 4) {
            System.err.println("ERROR: malformed location: " + location);
            return null;
        }
        String sourceFileName = loc[1];
        long startLine = Long.parseLong(loc[2]);
        // Use 1-based column numbering.
        long startCol = Long.parseLong(loc[3]) + 1;
        Position position = new Position(startLine, startLine, startCol, startCol + length);
        String artifact = artifactMap.get(sourceFileName);
        return new SourcePosition(artifact, sourceFileName, position);
    }

    private void process(String relationFile, Consumer<String[]> proc) {
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
    final String artifact;
    final String sourceFileName;
    final Position position;

    SourcePosition(String artifact, String sourceFileName, Position position) {
        this.artifact = artifact;
        this.sourceFileName = sourceFileName;
        this.position = position;
    }
}