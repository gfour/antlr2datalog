package org.clyze.antlr2datalog;

import java.util.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

/**
 * The AST visitor that records facts.
 */
public class FactVisitor {
    private final String fileId;
    private final Map<Class<?>, Rule> schema;
    private final Database db;

    /**
     * Create a new visitor to generate facts.
     * @param fileId   the source file id (unique per file)
     * @param schema   the database schema to use
     * @param db       the database object to use for writing
     */
    public FactVisitor(int fileId, Map<Class<?>, Rule> schema, Database db) {
        this.fileId = "#" + fileId + "#";
        this.schema = schema;
        this.db = db;
    }

    /**
     * Generates a unique id for an object returned by the parser, so that the
     * object can be identified in the facts of the whole program.
     * @param name     the name of the object type
     * @param obj      the object
     * @return         the unique id
     */
    public String getNodeId(String name, Object obj) {
        return obj == null ? "##null" : name + fileId + obj.hashCode();
    }

    /**
     * Visit a node in the AST.
     * @param typedParseTree   the node of the tree
     */
    public void visitParseTree(TypedParseTree typedParseTree) {
        String relName = SchemaFinder.getSimpleName(typedParseTree.c, schema);
        if (Main.debug)
            System.out.println("relName = " + relName);
        String nodeId = getNodeId(relName, typedParseTree.parseTree);
        db.writeRow("is" + relName, nodeId);
        Collection<Component> rules = schema.get(typedParseTree.c).components;
        if (rules == null) {
            System.out.println("WARNING: schema lacks " + relName);
            return;
        }
        List<TypedParseTree> subTrees = new LinkedList<>();
        for (Component rule : rules)
            visitComponent(typedParseTree, relName, nodeId, rule, subTrees);
        for (TypedParseTree subTree : subTrees)
            visitParseTree(subTree);
    }

    private void visitComponent(TypedParseTree tpt, String relName, String parentNodeId, Component comp, List<TypedParseTree> subTrees) {
        try {
            if (Main.debug)
                System.out.println("Invoking: " + tpt.c.getName() + "." + comp.name + "()");
            Object result;
            try {
                result = comp.getter.invoke(tpt.parseTree);
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
            if (comp.index) {
                List<? extends ParseTree> pts = (List<? extends ParseTree>)result;
                if (pts != null) {
                    int idx = 0;
                    for (ParseTree pt : pts)
                        if (pt != null)
                            visitPt(relName, comp, parentNodeId, new TypedParseTree(pt, comp.type), idx++, subTrees);
                }
            } else {
                if (Main.debug)
                    System.out.println("comp.name = " + comp.name + ", tpt: " + tpt.parseTree.getClass().getName());
                ParseTree pt = (ParseTree)result;
                if (pt != null)
                    visitPt(relName, comp, parentNodeId, new TypedParseTree(pt, comp.type), 0, subTrees);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void visitPt(String relName, Component comp, String parentNodeId,
                         TypedParseTree typedParseTree, int index, List<TypedParseTree> subTrees) {
        String compSimpleName = SchemaFinder.getSimpleName(comp.type, schema);
        String compNodeId = getNodeId(compSimpleName, typedParseTree.parseTree);
        StringBuilder sb = new StringBuilder().append(parentNodeId).append('\t').append(compNodeId);
        db.writeRow(BaseSchema.PARENT_OF, sb.toString());
        if (comp.index)
            sb.append("\t").append(index);
        if (comp.isTerminal) {
            Token token = ((TerminalNode) typedParseTree.parseTree).getSymbol();
            sb.append("\t").append(token.getText().replace('\t', ' ').replace('\n', ' '))
                    .append("\t").append(token.getLine())
                    .append("\t").append(token.getStartIndex())
                    .append("\t").append(token.getStopIndex())
                    .append("\t").append(token.getCharPositionInLine());
        }
        db.writeRow(relName + "_" + comp.name, sb.toString());
        subTrees.add(typedParseTree);
    }
}

