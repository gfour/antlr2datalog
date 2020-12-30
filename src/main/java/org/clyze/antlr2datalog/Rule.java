package org.clyze.antlr2datalog;

import java.util.Collection;

/**
 * A parse rule used in fact generation.
 */
public class Rule {
    /** The list of rule components (sub-rules/terminals). */
    final Collection<Component> components;
    /**
     * The simple name of the rule (initialized by reflection but may be
     * later optimized/simplified.
     */
    String simpleName;

    public Rule(Collection<Component> components, String simpleName) {
        this.components = components;
        this.simpleName = simpleName;
    }
}
