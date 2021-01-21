package org.clyze.antlr2datalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/** The database to use for writing source code facts. */
public class Database {
    private final Map<String, Collection<String>> tables;
    private final File outDir;

    /**
     * Create a new database.
     * @param tables      the data structure to hold data before writing
     * @param outDir      the output directory path
     */
    public Database(Map<String, Collection<String>> tables, File outDir) {
        this.tables = tables;
        this.outDir = outDir;
    }

    /**
     * Register a facts row.
     * @param relName   the name of the facts relation
     * @param line      the line to write in the corresponding facts file
     */
    public void writeRow(String relName, String line) {
        Collection<String> relLines = tables.get(relName);
        if (relLines == null)
            relLines = new LinkedList<>();
        relLines.add(line);
        tables.put(relName, relLines);
    }

    /**
     * Call this method when all facts have been gathered.
     */
    public void writeFacts() {
        if (!outDir.mkdirs())
            System.out.println("WARNING: directory already exists: " + outDir);
        for (Map.Entry<String, Collection<String>> entry : tables.entrySet()) {
            String relName = entry.getKey();
            try (FileWriter fw = new FileWriter(new File(outDir, relName + ".facts"))) {
                for (String line : tables.get(relName)) {
                    fw.write(line);
                    fw.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
