package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.Test;

public class RustTest extends LangTest {
    @Test
    public void testRust() throws IOException {
        String topSrcPath = (new File("grammars-v4/rust")).getCanonicalPath();
        Main.main(new String[] { "-l", "rust", "-i", topSrcPath + "/examples/deno_core_runtime.rs", "--relative-path",  topSrcPath, "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/DB_RUST_Identifier_NON_KEYWORD_IDENTIFIER.facts")).exists());
        assert functionDefinition("Method@examples/deno_core_runtime.rs@4373-4513", "register_op", "examples/deno_core_runtime.rs:423:9");
        assert functionArity("Method@examples/deno_core_runtime.rs@4373-4513", "2");
        assert variableDeclaration("TerminalNode@examples/deno_core_runtime.rs@10231-10231", "mod_id", "examples/deno_core_runtime.rs:949:50");
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.types.size() == 13;
        assert sm.functions.size() == 80;
    }
}
