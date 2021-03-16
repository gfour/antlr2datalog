package org.clyze.antlr2datalog;

import java.util.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.*;

/**
 * The AST visitor that records facts.
 */
public class FactVisitor {
    private final String fileId;
    private final Map<Class<?>, Rule> schemaRules;
    private final Database langDb;
    private final Database baseDb;

    /**
     * Create a new visitor to generate facts.
     * @param fileId   the source file id (unique per file)
     * @param langDb   the database object to use for writing language facts
     * @param baseDb the database object to use for writing facts across languages
     */
    public FactVisitor(String fileId, Database langDb, Database baseDb) {
        this.fileId = "@" + fileId + "@";
        this.schemaRules = langDb.schema.rules;
        this.langDb = langDb;
        this.baseDb = baseDb;
    }

    /**
     * Generates a unique id for an object returned by the parser, so that the
     * object can be identified in the facts of the whole program.
     * @param name     the name of the object type
     * @param parseTree      the object
     * @return         the unique id
     */
    public String getNodeId(String name, ParseTree parseTree) {
        if (parseTree == null)
            return "##null";
        Interval sourceInterval = parseTree.getSourceInterval();
        String location = sourceInterval == null ? "unknown" : sourceInterval.a + "-" + sourceInterval.b;
        return name + fileId + location;
    }

    /**
     * Visit a node in the AST.
     * @param typedParseTree   the node of the tree
     * @return                 the unique id of the node
     */
    public String visitParseTree(TypedParseTree typedParseTree) {
        String relName = SchemaFinder.getSimpleName(typedParseTree.c, schemaRules);
        if (Main.debug)
            System.out.println("Visiting: " + typedParseTree.id + ", relName = " + relName);
        String nodeId = getNodeId(relName, typedParseTree.parseTree);
        langDb.writeRow("is" + relName, nodeId);
        Collection<Component> rules = schemaRules.get(typedParseTree.c).components;
        if (rules == null) {
            System.out.println("WARNING: schema lacks " + relName);
            return null;
        }
        List<TypedParseTree> subTrees = new LinkedList<>();
        for (Component rule : rules)
            visitComponent(typedParseTree, relName, nodeId, rule, subTrees);
        for (TypedParseTree subTree : subTrees)
            visitParseTree(subTree);
        return nodeId;
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
            if (Main.debug)
                System.out.println("comp.name = " + comp.name + ", comp.index = " + comp.index + ", tpt: " + tpt.parseTree.getClass().getName() + ", result = " + result);
            if (comp.index) {
                List<? extends ParseTree> pts = (List<? extends ParseTree>)result;
                if (pts != null) {
                    int idx = 0;
                    for (ParseTree pt : pts)
                        if (pt != null)
                            visitPt(relName, comp, parentNodeId, new TypedParseTree(pt, comp.type), idx++, subTrees);
                }
            } else {
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
        String compSimpleName = SchemaFinder.getSimpleName(comp.type, schemaRules);
        ParseTree pTree = typedParseTree.parseTree;
        String compNodeId = getNodeId(compSimpleName, pTree);
        if (Main.debug)
            System.out.println("compSimpleName = " + compSimpleName + ", compNodeId = " + compNodeId + ", class = " + pTree.getClass().getSimpleName());
        StringBuilder sb = new StringBuilder().append(parentNodeId).append('\t').append(compNodeId);
        BaseSchema.writeParentOf(baseDb, sb.toString());
        if (comp.index)
            sb.append("\t").append(index);
        if (comp.isTerminal) {
            Token token = ((TerminalNode) pTree).getSymbol();
            String text = token.getText();
            int line = token.getLine();
            if (Main.debug)
                System.out.println("Token: " + text + ", line = " + line);
            if (text == null) {
                System.out.println("WARNING: null token.");
                text = "";
            }
            String sbTerminal = compNodeId +
                    "\t" + text.replace('\t', ' ').replace('\n', ' ') +
                    "\t" + line +
                    "\t" + token.getStartIndex() +
                    "\t" + token.getStopIndex() +
                    "\t" + token.getCharPositionInLine();
            BaseSchema.writeTerminal(baseDb, sbTerminal);
        }
        langDb.writeRow(relName + "_" + comp.name, sb.toString());
        if (Main.debug) {
            System.out.println("Adding node [" + compNodeId + "] to subTrees.");
            typedParseTree.id = compNodeId;
        }
        subTrees.add(typedParseTree);

        // If this value is a non-terminal and is in fact of another type,
        // also process the getters of that type.
        if (typedParseTree.subTyped) {
            Class<? extends ParseTree> pClass = pTree.getClass();
            if (TerminalNode.class.isAssignableFrom(pClass))
                return;
            if (Main.debug)
                System.out.println("Also processing [" + compNodeId + "] as " + pClass.getSimpleName());
            String subNodeId = visitParseTree(new TypedParseTree(pTree, pClass));
            BaseSchema.writeParentOf(baseDb, compNodeId + '\t' + subNodeId);
        }
    }
}

