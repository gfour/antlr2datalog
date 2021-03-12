package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.Test;

public class CppTest extends LangTest {
    @Test
    public void testCpp() {
        Main.main(new String[] { "-l", "cpp", "-i", "grammars-v4/cpp/examples/and_keyword.cpp" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }
}
