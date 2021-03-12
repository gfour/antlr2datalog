package org.clyze.antlr2datalog;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test simultaneous processing of many source files in different languages.
 * This tests that the combined parsing/analysis does not fail (e.g. due to
 * same-name relations in different languages).
 */
public class MultiLangMode extends LangTest {
    @Test
    public void test() {
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("-l", "cobol85", "-i", Cobol85Test.SRC_FILE));
        args.addAll(Arrays.asList("-l", "c", "-i", CTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "cpp", "-i", CppTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "go", "-i", GoTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "kotlin", "-i", KotlinTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "lua", "-i", LuaTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "python3", "-i", Python3Test.SRC_FILE));
        args.addAll(Arrays.asList("-l", "rust", "-i", RustTest.SRC_FILE));
        args.addAll(Arrays.asList("-l", "solidity", "-i", SolidityTest.SRC_FILE));
        args.add("--generate-metadata");
        Main.main(args.toArray(new String[0]));
        assert metadataExist();
    }
}
