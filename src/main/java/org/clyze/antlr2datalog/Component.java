package org.clyze.antlr2datalog;

import org.antlr.v4.runtime.tree.ParseTree;

/** A parser rule component (sub-rule or terminal). */
class Component {
    /** The name of the getter method for the rule. */
    final String name;
    /** The return type of the sub-rule/terminal getter. */
    final Class<? extends ParseTree> type;
    /** True if the getter takes an index (and thus another getter exists that returns a list). */
    final boolean index;
    /** True if this is a terminal node. */
    final boolean isTerminal;

    Component(String name, Class<? extends ParseTree> type, boolean index, boolean isTerminal) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.isTerminal = isTerminal;
    }
}
