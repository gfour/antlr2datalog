package org.clyze.antlr2datalog;

import java.util.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

/**
 * The AST visitor that records facts.
 */
public class FactVisitor {
    private final int fileId;
    private final Map<Class<?>, Rule> schema;
    private final Database db;

    /**
     * Create a new visitor to generate facts.
     * @param fileId   the source file id (unique per file)
     * @param schema   the database schema to use
     * @param db       the database object to use for writing
     */
    public FactVisitor(int fileId, Map<Class<?>, Rule> schema, Database db) {
        this.fileId = fileId;
        this.schema = schema;
        this.db = db;
    }

    private String getNodeId(String name, Object obj) {
        return obj == null ? "null" : name + "#" + fileId + "#" + obj.hashCode();
    }

    public void visitParseTree(TypedParseTree typedParseTree) {
        Class<? extends ParseTree> c = typedParseTree.c;
        String relName = c.getSimpleName();
        if (Main.debug)
            System.out.println("relName = " + relName);
        String nodeId = getNodeId(relName, typedParseTree.parseTree);
        db.writeRow("is" + relName, nodeId);
        Rule r = schema.get(c);
        if (r == null) {
            System.out.println("WARNING: schema lacks " + relName);
            System.exit(0);
            return;
        }
        List<TypedParseTree> subTrees = new LinkedList<>();
        for (Component rule : r.rules)
            visitComponent(typedParseTree.parseTree, c, relName, nodeId, rule, subTrees);
        for (TypedParseTree subTree : subTrees)
            visitParseTree(subTree);
    }

    private void visitComponent(ParseTree parseTree, Class<? extends ParseTree> c, String relName, String nodeId, Component comp, List<TypedParseTree> subTrees) {
        try {
            if (Main.debug)
                System.out.println("Invoking: " + c.getName() + "." + comp.name + "()");
            if (comp.index) {
                List<? extends ParseTree> pts = (List<? extends ParseTree>)c.getDeclaredMethod(comp.name).invoke(parseTree);
                if (pts != null) {
                    int idx = 0;
                    for (ParseTree pt : pts)
                        if (pt != null)
                            visitPt(relName, comp, nodeId, new TypedParseTree(pt, comp.type), idx++, subTrees);
                }
            } else {
                if (Main.debug)
                    System.out.println("c = " + c.getName() + ", comp.name = " + comp.name + ", tpt: " + parseTree.getClass().getName());
                ParseTree pt = (ParseTree) c.getDeclaredMethod(comp.name).invoke(parseTree);
                if (pt != null)
                    visitPt(relName, comp, nodeId, new TypedParseTree(pt, comp.type), 0, subTrees);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void visitPt(String relName, Component comp, String nodeId,
                         TypedParseTree typedParseTree, int index, List<TypedParseTree> subTrees) {
        String nodeId0 = getNodeId(comp.type.getSimpleName(), typedParseTree.parseTree);
        String suffix = comp.index ? ("\t" + index) : "";
        if (comp.isTerminal) {
            Token token = ((TerminalNode) typedParseTree.parseTree).getSymbol();
            suffix += ("\t" + token.getText().replace('\t', ' ').replace('\n', ' '));
            suffix += ("\t" + token.getLine() + "\t" +  token.getStartIndex() + "\t" + token.getStopIndex() + "\t" + token.getCharPositionInLine());
        }
        db.writeRow(relName + "_" + comp.name, nodeId + '\t' + nodeId0 + suffix);
        if (typedParseTree != null)
            subTrees.add(typedParseTree);
    }
}

