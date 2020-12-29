package org.clyze.antlr2datalog;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * A parse tree accompanied by a supertype of its type. This is used to remember
 * return types for reflectively-discovered getters even if the actual returned
 * values have a different subtype.
 */
class TypedParseTree {
    /** The parse tree object. */
    final ParseTree parseTree;
    /** The companion (super)type. */
    final Class<? extends ParseTree> c;
    /** The simple name of the type. */
    final String simpleName;

    TypedParseTree(ParseTree parseTree, Class<? extends ParseTree> c) {
        this.parseTree = parseTree;
        this.c = c;
        this.simpleName = c.getSimpleName();
    }
}
