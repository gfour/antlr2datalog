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
public class SchemaFinder {
    private final Collection<Class<? extends ParseTree>> visitedRules = new HashSet<>();
    /** The computed schema. */
    public final Map<Class<?>, Rule> schema = new HashMap<>();
    private static final Class<RuleNode> RULE_NODE_CLASS = RuleNode.class;
    private static final Class<TerminalNode> TERMINAL_NODE_CLASS = TerminalNode.class;

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
        Rule cRule = new Rule();
        for (Method m : c.getDeclaredMethods()) {
            Class<?> retType = m.getReturnType();
            if (RULE_NODE_CLASS.isAssignableFrom(retType)) {
                if (Main.debug)
                    System.out.println("+ Rule method: " + m.getName() + " with type " + retType.getName());
                registerComponent(m, retType, cRule, false, next);
            } else if (TERMINAL_NODE_CLASS.isAssignableFrom(retType)) {
                if (Main.debug)
                    System.out.println("+ Terminal method: " + m.getName() + " with type " + retType.getName());
                registerComponent(m, retType, cRule, true, next);
            }
        }
        if (Main.debug)
            System.out.println("\\-> Recording " + c.getSimpleName());
        schema.put(c, cRule);
        visitedRules.add(c);
        for (Class<? extends ParseTree> c0 : next)
            discoverSchema(c0);
    }

    private void registerComponent(Method m, Class<?> retType, Rule cRule, boolean isTerminal, Collection<Class<? extends ParseTree>> next) {
        Class<? extends ParseTree> retParseTreeType = (Class<? extends ParseTree>)retType;
        boolean index = isIndex(m.getParameterCount());
        if (index) {
            try {
                m = m.getDeclaringClass().getDeclaredMethod(m.getName());
            } catch (NoSuchMethodException e) {
                System.out.println("WARNING: method " + m + " does not have a no-arg version.");
                e.printStackTrace();
                return;
            }
        }
        cRule.rules.add(new Component(retParseTreeType, index, isTerminal, m));
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
     * Writes the schema to a Datalog file.
     * @param schemaFile   the output file
     */
    public void printSchema(File schemaFile) {
        StringBuilder sbSchema = new StringBuilder();
        for (Class<?> key : schema.keySet()) {
            sbSchema.append(".type ");
            sbSchema.append(key.getSimpleName());
            sbSchema.append(" = symbol\n");
        }
        sbSchema.append(".decl Source_File_Id(filename: symbol, id: symbol)\n");
        sbSchema.append(".input Source_File_Id\n");
        for (Map.Entry<Class<?>, Rule> relation : schema.entrySet()) {
            Rule r = relation.getValue();
            String relName = relation.getKey().getSimpleName();
            // Relation isX
            String relName0 = "is" + relName;
            sbSchema.append(".decl " );
            sbSchema.append(relName0);
            sbSchema.append("(node_id:symbol)\n");
            sbSchema.append(".input ");
            sbSchema.append(relName0);
            sbSchema.append('\n');
            // Relations X_component
            for (Component comp : r.rules) {
                String relNameC = relName + "_" + comp.name;
                sbSchema.append(".decl " );
                sbSchema.append(relNameC);
                sbSchema.append("(node_id:symbol, ");
                sbSchema.append(comp.name).append(":").append(comp.type.getSimpleName());
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
}
