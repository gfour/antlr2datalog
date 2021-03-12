package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.Test;

public class CppTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/cpp/examples/and_keyword.cpp";

    @Test
    public void testCpp() {
        Main.main(new String[] { "-l", "cpp", "-i", SRC_FILE });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }
}
