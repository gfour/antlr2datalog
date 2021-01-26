package org.clyze.antlr2datalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Schema {
    /** The language of the schema. */
    private final String language;
    /** The Datalog text of the schema. */
    protected final StringBuilder textBuilder;
    /** The list of all relations (so that they can be later initialized). */
    public final List<String> relations;
    public final String relationPrefix;
    public final Map<Class<?>, Rule> rules;

    public Schema(String language, StringBuilder textBuilder, List<String> relations, Map<Class<?>, Rule> rules) {
        this.language = language;
        this.textBuilder = textBuilder;
        this.relations = relations;
        this.relationPrefix = language == null ? "" : ("DB_" + language + "_");
        this.rules = rules;
    }

    /**
     * Writes a list of Datalog schema descriptions to a logic file.
     * @param langSchemas  the list of language schemas to write (as components)
     * @param schemaFile   the output file
     * @throws IOException when writing fails
     */
    public static void write(Schema baseSchema, List<Schema> langSchemas, File schemaFile) throws IOException {
        try (FileWriter fw = new FileWriter(schemaFile)) {

            // Guard against multiple inclusion.
            fw.write("#pragma once\n");

            // Write base schema, common for all languages.
            fw.write(baseSchema.textBuilder.toString());
            fw.write("\n");

            // Write one schema per language, as a different component.
            for (Schema langSchema : langSchemas) {
                if (Main.debug)
                    System.out.println("Writing schema for: " + langSchema.language);
                String component = langSchema.relationPrefix;
                fw.write(".comp " + component + " {\n");
                fw.write(langSchema.textBuilder.toString());
                for (String relation : langSchema.relations) {
                    fw.write(".input " + relation + "(filename=\"" + component + relation + ".facts\")\n");
                }
                fw.write("}\n");
                fw.write(".init db_" + langSchema.language + " = " + component + "\n");
            }
        }
    }
}
