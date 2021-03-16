package org.clyze.antlr2datalog;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * A parse tree accompanied by a supertype of its type. This is used to remember
 * return types for reflectively-discovered getters even if the actual returned
 * values have a different subtype.
 */
public class TypedParseTree {
    /** The parse tree object. */
    final ParseTree parseTree;
    /** The companion (super)type. */
    final Class<? extends ParseTree> c;
    /** If true, this parse tree is of a different type than the supertype it presents. */
    final boolean subTyped;
    /** The id of this parse tree, used in debug reports. */
    String id = null;

    TypedParseTree(ParseTree parseTree, Class<? extends ParseTree> c, boolean subTyped) {
        this.parseTree = parseTree;
        this.c = c;
        this.subTyped = subTyped;
    }

    TypedParseTree(ParseTree parseTree, Class<? extends ParseTree> c) {
        this(parseTree, c, parseTree.getClass() != c);
    }
}
