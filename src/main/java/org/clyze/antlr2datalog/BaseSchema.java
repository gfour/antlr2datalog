package org.clyze.antlr2datalog;

/**
 * Base relations that every schema should contain.
 */
public class BaseSchema {
    /** The relation mapping the root node to the source file path. */
    public static final String SOURCE_FILE_ID = "BASE_SourceFileId";
    /** Relation node-is-parent-of-node. */
    public static final String PARENT_OF = "BASE_ParentOf";
}
