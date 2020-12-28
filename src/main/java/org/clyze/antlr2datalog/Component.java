package org.clyze.antlr2datalog;

import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/** A parser rule component (sub-rule or terminal). */
class Component {
    /** The object to use to look up getter method handles. */
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    /** The name of the getter method for the rule. */
    final String name;
    /** The return type of the sub-rule/terminal getter. */
    final Class<? extends ParseTree> type;
    /** True if the getter takes an index (and thus another getter exists that returns a list). */
    final boolean index;
    /** True if this is a terminal node. */
    final boolean isTerminal;
    /** The getter returning the sub-rule/terminal. */
    final MethodHandle getter;

    Component(Class<? extends ParseTree> type, boolean index, boolean isTerminal, Method getter) {
        this.name = getter.getName();
        this.type = type;
        this.index = index;
        this.isTerminal = isTerminal;
        this.getter = toMethodHandle(getter);
    }

    private static final MethodHandle toMethodHandle(Method m) {
        try {
            return lookup.unreflect(m);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
