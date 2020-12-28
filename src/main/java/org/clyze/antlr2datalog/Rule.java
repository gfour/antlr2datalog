package org.clyze.antlr2datalog;

import java.util.Collection;
import java.util.HashSet;

/** A parser rule. */
class Rule {
    /** The list of component in this rule (other rules or terminals). */
    final Collection<Component> rules = new HashSet<>();
}
