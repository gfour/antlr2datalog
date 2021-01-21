package org.clyze.antlr2datalog;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
//import static org.junit.Assert.*;

public class MainTest {
    @Before
    public void deleteWorkspace() {
        FileUtils.deleteQuietly(new File(Main.DEFAULT_WORKSPACE));
    }

    @Test public void modePython3() {
        Main.main(new String[] { "-l", "python3", "-i", "grammars-v4/python/python3/examples/coroutines.py", "--compile" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/Function.csv")).exists());
    }

    @Test public void modeKotlin() {
        Main.main(new String[] { "-l", "kotlin", "-i", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
    }

    @Test public void modeCobol85() {
        Main.main(new String[] { "-l", "cobol85", "-i", "grammars-v4/cobol85/examples/example1.txt" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/StringConstant.csv")).exists());
    }

    @Test public void modeLua() {
        Main.main(new String[] { "-l", "lua", "-i", "grammars-v4/lua/examples/factorial.lua" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/Var_NAME.facts")).exists());
    }

    @Test public void modeRust() {
        deleteWorkspace();
        Main.main(new String[] { "-l", "rust", "-i", "grammars-v4/rust/examples/deno_core_runtime.rs" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/Identifier_NON_KEYWORD_IDENTIFIER.facts")).exists());
    }
}
