package org.clyze.antlr2datalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** The database to use for writing source code facts. */
public class Database {
    public final Schema schema;
    private final Map<String, List<String>> tables;
    private final File outDir;
    private final String relationPrefix;

    /**
     * Create a new database.
     * @param schema      the schema of the database
     * @param outDir      the output directory path
     */
    public Database(Schema schema, File outDir) {
        this.schema = schema;
        this.outDir = outDir;
        this.tables = new HashMap<>();
        for (String relation : schema.relations)
            tables.put(relation, new LinkedList<>());
        this.relationPrefix = schema.relationPrefix;
    }

    /**
     * Register a facts row.
     * @param relName   the name of the facts relation
     * @param line      the line to write in the corresponding facts file
     */
    public void writeRow(String relName, String line) {
        List<String> relLines = tables.get(relName);
        if (relLines == null) {
            System.out.println("WARNING: input relation '" + relName + "' not initialized properly, it may be missing for other inputs.");
            relLines = new LinkedList<>();
        }
        relLines.add(line);
        tables.put(relName, relLines);
    }

    /**
     * Call this method when all facts have been gathered.
     * @param debug    debugging mode (diagnostics)
     */
    public void writeFacts(boolean debug) {
        if (!outDir.mkdirs() && debug)
            System.out.println("WARNING: directory already exists: " + outDir);
        for (Map.Entry<String, List<String>> entry : tables.entrySet()) {
            String relName = entry.getKey();
            try (FileWriter fw = new FileWriter(new File(outDir, relationPrefix + relName + ".facts"))) {
                List<String> lines = tables.get(relName);
                Collections.sort(lines);
                if (lines.isEmpty()) {
                    if (Main.debug)
                        System.out.println("WARNING: empty relation " + relName);
                } else
                    for (String line : lines) {
                        fw.write(line);
                        fw.write("\n");
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
