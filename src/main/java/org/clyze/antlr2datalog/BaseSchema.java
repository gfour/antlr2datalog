package org.clyze.antlr2datalog;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Base relations that every schema should contain.
 */
public class BaseSchema extends Schema {
    /** The relation mapping the root node to the source file path. */
    private static final String SOURCE_FILE_ID = "BASE_SourceFileId";
    /** Relation node-is-parent-of-node. */
    private static final String PARENT_OF = "BASE_ParentOf";
    /** Relation containing all terminals. */
    private static final String TERMINAL = "BASE_Terminal";

    private BaseSchema(StringBuilder textBuilder, List<String> relations) {
        super(null, textBuilder, relations, new HashMap<>());
    }

    /**
     * Generates the base schema shared by all analyses.
     * @return   the schema object
     */
    public static BaseSchema create() {
        List<String> relationNames = new LinkedList<>();
        StringBuilder sbSchema = new StringBuilder();
        sbSchema.append(".decl ").append(SOURCE_FILE_ID).append("(filename: symbol, file_id: symbol, node_id: symbol)\n");
        sbSchema.append(".input ").append(SOURCE_FILE_ID).append('\n');
        relationNames.add(SOURCE_FILE_ID);
        sbSchema.append(".decl ").append(PARENT_OF).append("(id: symbol, parent_id: symbol)\n");
        sbSchema.append(".input ").append(PARENT_OF).append('\n');
        relationNames.add(PARENT_OF);
        sbSchema.append(".decl ").append(TERMINAL).append("(id: symbol, text:symbol, line: number, startIndex: number, stopIndex: number, charPos: number)\n");
        sbSchema.append(".input ").append(TERMINAL).append('\n');
        relationNames.add(TERMINAL);
        Collections.sort(relationNames);
        return new BaseSchema(sbSchema, relationNames);
    }

    /**
     * Writes a parent-of tuple in the database.
     * @param baseDb   the database object
     * @param line     the text line representation of the tuple
     */
    public static void writeParentOf(Database baseDb, String line) {
        baseDb.writeRow(PARENT_OF, line);
    }

    /**
     * Writes a source-file-id tuple in the database.
     * @param baseDb   the database object
     * @param line     the text line representation of the tuple
     */
    public static void writeSourceFileId(Database baseDb, String line) {
        baseDb.writeRow(SOURCE_FILE_ID, line);
    }

    /**
     * Writes a terminal node in the database.
     * @param baseDb   the database object
     * @param line     the text line representation of the terminal node tuple
     */
    public static void writeTerminal(Database baseDb, String line) {
        baseDb.writeRow(TERMINAL, line);
    }
}
