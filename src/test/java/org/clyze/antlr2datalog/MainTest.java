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

    @Test public void testPython3() {
        Main.main(new String[] { "-l", "python3", "-i", "grammars-v4/python/python3/examples/coroutines.py", "--compile" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testKotlin() {
        Main.main(new String[] { "-l", "kotlin", "-i", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
    }

    @Test public void testCobol85() {
        Main.main(new String[] { "-l", "cobol85", "-i", "src/test/resources/InputSORT.cbl" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_StringConstant.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testLua() {
        Main.main(new String[] { "-l", "lua", "-i", "grammars-v4/lua/examples/factorial.lua" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/Var_NAME.facts")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testRust() {
        Main.main(new String[] { "-l", "rust", "-i", "grammars-v4/rust/examples/deno_core_runtime.rs" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/Identifier_NON_KEYWORD_IDENTIFIER.facts")).exists());
    }

    @Test public void testC() {
        Main.main(new String[] { "-l", "c", "-i", "grammars-v4/c/examples/FuncForwardDeclaration.c" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testCpp() {
        Main.main(new String[] { "-l", "cpp", "-i", "grammars-v4/cpp/examples/and_keyword.cpp" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }
}
