package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
//import static org.junit.Assert.*;

public class MainTest {
    @Before
    public void deleteWorkspace() {
        FileUtils.deleteQuietly(new File(Main.DEFAULT_WORKSPACE));
    }

    @Test public void testC() {
        Main.main(new String[] { "-l", "c", "-i", "grammars-v4/c/examples/FuncForwardDeclaration.c", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assertMetadataExist();
    }

    @Test public void testCpp() {
        Main.main(new String[] { "-l", "cpp", "-i", "grammars-v4/cpp/examples/and_keyword.cpp" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testCobol85() {
        Main.main(new String[] { "-l", "cobol85", "-i", "src/test/resources/InputSORT.cbl" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_StringConstant.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testGo() {
        Main.main(new String[] { "-l", "go", "-i", "src/test/resources/bit_cmd.go", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_Function_Area.csv")).exists());
        assertMetadataExist();
    }

    @Test public void testKotlin() {
        Main.main(new String[] { "-l", "kotlin", "-i", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
    }

    @Test public void testLua() {
        Main.main(new String[] { "-l", "lua", "-i", "grammars-v4/lua/examples/factorial.lua" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/DB_LUA_Var__NAME.facts")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testPython3() {
        Main.main(new String[] { "-l", "python3", "-i", "grammars-v4/python/python3/examples/coroutines.py", "--compile" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }

    @Test public void testRust() throws IOException {
        String topSrcPath = (new File("grammars-v4/rust")).getCanonicalPath();
        Main.main(new String[] { "-l", "rust", "-i", topSrcPath + "/examples/deno_core_runtime.rs", "--relative-path",  topSrcPath, "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/DB_RUST_Identifier_NON_KEYWORD_IDENTIFIER.facts")).exists());
        assertMetadataExist();
    }

    @Test public void testSolidity() {
        Main.main(new String[] { "-l", "solidity", "-i", "./grammars-v4/solidity/test.sol", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assertMetadataExist();
    }

    private void assertMetadataExist() {
        assert((new File(Main.DEFAULT_WORKSPACE, "database/" + MetadataGenerator.OUTPUT_FILE).exists()));
    }
}
