package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.clyze.persistent.metadata.JSONUtil;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.Before;
import org.junit.Test;
//import static org.junit.Assert.*;

public class MainTest {
    @Before
    public void deleteWorkspace() {
        FileUtils.deleteQuietly(new File(Main.DEFAULT_WORKSPACE));
    }

    @Test public void testC() throws IOException {
        Main.main(new String[] { "-l", "c", "-i", "grammars-v4/c/examples/FuncForwardDeclaration.c", "-i", "grammars-v4/c/examples/ll.c", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assertMetadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.functions.size() == 2;
        assert sm.types.size() == 1;
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

    @Test public void testGo() throws IOException {
        Main.main(new String[] { "-l", "go", "-i", "src/test/resources/bit_cmd.go", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_Function_Area.csv")).exists());
        assert functionDefinition("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "HijackGitCommandOccurred", "src/test/resources/bit_cmd.go:134:5");
        assert functionArity("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "3");
        assertMetadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.functions.size() == 9;
        assert sm.types.size() == 0;
    }

    @Test public void testKotlin() {
        Main.main(new String[] { "-l", "kotlin", "-i", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
        assert functionDefinition("FunctionDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@4298-4353", "main", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:559:4");
        assert functionArity("FunctionDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@4298-4353", "1");
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
        assert functionDefinition("Method@examples/deno_core_runtime.rs@4373-4513", "register_op", "examples/deno_core_runtime.rs:423:9");
        assert functionArity("Method@examples/deno_core_runtime.rs@4373-4513", "2");
        assertMetadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.types.size() == 13;
        assert sm.functions.size() == 80;
    }

    @Test public void testSolidity() {
        Main.main(new String[] { "-l", "solidity", "-i", "./grammars-v4/solidity/test.sol", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assertMetadataExist();

    }

    private void assertMetadataExist() {
        assert(getMetadataFile().exists());
    }

    private static File getDatabase() {
        return new File(Main.DEFAULT_WORKSPACE, "database");
    }

    private static File getMetadataFile() {
        return new File(getDatabase(), MetadataGenerator.OUTPUT_FILE);
    }

    private static SourceMetadata getSourceMetadata() throws IOException {
        return SourceMetadata.fromMap(JSONUtil.toMap(getMetadataFile().toPath()));
    }

    private static boolean relationTuple(String relName, java.util.function.Function<String[], Boolean> test) {
        File csv = new File(getDatabase(), relName);
        try {
            return Files.lines(csv.toPath()).map((String line) -> line.split("\t")).anyMatch(test::apply);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean functionArity(String funcId, String arity) {
        return relationTuple("BASE_Function_Arity.csv", ((String[] parts) -> parts[0].equals(funcId) && parts[1].equals(arity)));
    }

    private boolean functionDefinition(String funcId, String name, String loc) {
        return relationTuple("BASE_FunctionDefinition.csv", ((String[] parts) ->
                (parts[0].equals(funcId)) && (parts[1].equals(name)) && (parts[2].equals(loc))));
    }
}
