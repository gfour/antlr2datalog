package org.clyze.antlr2datalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class supports traversing the ANTLR parser API to understand the grammar,
 * in order to generate a database schema for the output Datalog facts.
 */
public final class SchemaFinder {
    private static final Class<RuleNode> RULE_NODE_CLASS = RuleNode.class;
    private static final Class<TerminalNode> TERMINAL_NODE_CLASS = TerminalNode.class;
    private final Collection<Class<? extends ParseTree>> visitedRules = new HashSet<>();
    private final ParserConfiguration parserConfiguration;
    /** The computed schema. */
    public final Map<Class<?>, Rule> schema = new HashMap<>();

    public SchemaFinder(ParserConfiguration parserConfiguration) {
        this.parserConfiguration = parserConfiguration;
    }

    /**
     * Discovers the facts schema starting from an API node.
     * @param c     the Class of the API node
     */
    public void discoverSchema(Class<? extends ParseTree> c) {
        if (visitedRules.contains(c))
            return;

        Collection<Class<? extends ParseTree>> next = new LinkedList<>();
        if (Main.debug)
            System.out.println("Processing class: " + c.getSimpleName());
        Collection<Component> cRules = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            Class<?> retType = m.getReturnType();
            if (RULE_NODE_CLASS.isAssignableFrom(retType)) {
                if (Main.debug)
                    System.out.println("+ Rule method: " + m.getName() + " with type " + retType.getName());
                registerComponent(m, c, retType, cRules, false, next);
            } else if (TERMINAL_NODE_CLASS.isAssignableFrom(retType)) {
                if (Main.debug)
                    System.out.println("+ Terminal method: " + m.getName() + " with type " + retType.getName());
                registerComponent(m, c, retType, cRules, true, next);
            }
        }
        if (Main.debug)
            System.out.println("\\-> Recording " + c.getSimpleName());
        schema.put(c, new Rule(cRules, c.getSimpleName()));
        visitedRules.add(c);
        for (Class<? extends ParseTree> c0 : next)
            discoverSchema(c0);
    }

    private void registerComponent(Method m, Class<? extends ParseTree> c,
                                   Class<?> retType, Collection<Component> cRules,
                                   boolean isTerminal, Collection<Class<? extends ParseTree>> next) {
        Class<? extends ParseTree> retParseTreeType = (Class<? extends ParseTree>)retType;
        boolean index = isIndex(m.getParameterCount());
        if (index) {
            try {
                m = c.getDeclaredMethod(m.getName());
            } catch (NoSuchMethodException e) {
                System.out.println("WARNING: method " + m + " does not have a no-arg version.");
                e.printStackTrace();
                return;
            }
        }
        try {
            cRules.add(new Component(retParseTreeType, index, isTerminal, m));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (!visitedRules.contains(retType))
            next.add(retParseTreeType);

    }

    private boolean isIndex(int arity) {
        if (arity == 0)
            return false;
        else if (arity == 1)
            return true;
        else
            throw new RuntimeException("Bad arity: " + arity);
    }

    /**
     * Simplify relation names in the schema by removing the "Context" suffix.
     */
    private void simplifyNames() {
        Set<String> originalSimpleNames = new HashSet<>();
        for (Rule r : schema.values())
            originalSimpleNames.add(r.simpleName);
        for (Map.Entry<Class<?>, Rule> entry : schema.entrySet()) {
            Rule r = entry.getValue();
            String sn = r.simpleName;
            final String CONTEXT_SUFFIX = "Context";
            if (sn.endsWith(CONTEXT_SUFFIX)) {
                String newName = sn.substring(0, sn.length() - CONTEXT_SUFFIX.length());
                if (!originalSimpleNames.contains(newName)) {
                    if (Main.debug)
                        System.out.println("Renaming relation: " + sn + " -> " + newName);
                    r.simpleName = newName;
                }
            }
        }
    }

    /**
     * Writes the schema to a Datalog file.
     * @param schemaFile   the output file
     */
    public void printSchema(File schemaFile) {
        simplifyNames();
        StringBuilder sbSchema = new StringBuilder();
        for (Class<?> key : schema.keySet()) {
            sbSchema.append(".type ");
            sbSchema.append(getSimpleName(key, schema));
            sbSchema.append(" = symbol\n");
        }
        sbSchema.append(".decl ").append(BaseSchema.SOURCE_FILE_ID).append("(filename: symbol, file_id: symbol, node_id: symbol)\n");
        sbSchema.append(".input ").append(BaseSchema.SOURCE_FILE_ID).append('\n');
        sbSchema.append(".decl ").append(BaseSchema.PARENT_OF).append("(id: symbol, parent_id: symbol)\n");
        sbSchema.append(".input ").append(BaseSchema.PARENT_OF).append('\n');
        for (Map.Entry<Class<?>, Rule> relation : schema.entrySet()) {
            Rule r = relation.getValue();
            Collection<Component> rules = r.components;
            String relName = getSimpleName(relation.getKey(), schema);
            // Relation isX
            String relName0 = "is" + relName;
            sbSchema.append(".decl " );
            sbSchema.append(relName0);
            sbSchema.append("(node_id:symbol)\n");
            sbSchema.append(".input ");
            sbSchema.append(relName0);
            sbSchema.append('\n');
            // Relations X_component
            for (Component comp : rules) {
                String relNameC = relName + "_" + comp.name;
                sbSchema.append(".decl " );
                sbSchema.append(relNameC);
                sbSchema.append("(node_id:symbol, ");
                sbSchema.append(comp.name).append(":").append(getSimpleName(comp.type, schema));
                if (comp.index)
                    sbSchema.append(", _comp_index: number");
                if (comp.isTerminal)
                    sbSchema.append(", _comp_text: symbol, line: number, startIndex: number, stopIndex: number, charPos: number");
                sbSchema.append(")\n");
                sbSchema.append(".input ").append(relNameC).append('\n');
            }
        }
        try (FileWriter fw = new FileWriter(schemaFile)) {
            fw.write(sbSchema.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Compute the logic schema from the underlying parser configuration.
     * @return     the logic schema (parser rule class to a collection of sub-rules/terminals)
     */
    public Map<Class<?>, Rule> computeSchema() {
        Class<? extends ParseTree> rootNodeClass = (Class<? extends ParseTree>)parserConfiguration.rootNodeMethod.getReturnType();
        discoverSchema(rootNodeClass);
        return schema;
    }

    public static String getSimpleName(Class<?> c, Map<Class<?>, Rule> schema) {
        Rule r = schema.get(c);
        return (r == null) ? c.getSimpleName() : r.simpleName;
    }
}
