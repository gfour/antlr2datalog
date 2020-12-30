package org.clyze.antlr2datalog;

import java.util.Collection;

/**
 * A parse rule used in fact generation.
 */
public class Rule {
    /** The list of rule components (sub-rules/terminals). */
    public final Collection<Component> components;
    /**
     * The simple name of the rule (initialized by reflection but may be
     * later optimized/simplified.
     */
    public String simpleName;

    /**
     * Create the description of a new parse rule.
     * @param components    the components of the rule
     * @param simpleName    the simple name of the rule
     */
    public Rule(Collection<Component> components, String simpleName) {
        this.components = components;
        this.simpleName = simpleName;
    }
}
