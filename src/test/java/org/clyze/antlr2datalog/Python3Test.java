package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class Python3Test extends LangTest {
    public static final String SRC_FILE = "grammars-v4/python/python3/examples/coroutines.py";

    @Test
    public void testPython3() {
        Main.main(new String[] { "-l", "python3", "-i", SRC_FILE, "--compile", "--profile" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert functionDefinition("Funcdef@grammars-v4/python/python3/examples/coroutines.py@1048-1159", "coro", ":grammars-v4/python/python3/examples/coroutines.py:209:12");
        assert functionArity("Funcdef@grammars-v4/python/python3/examples/coroutines.py@1048-1159", "2");
        assert variableDeclaration("TerminalNode@grammars-v4/python/python3/examples/coroutines.py@1052-1052", "args", ":grammars-v4/python/python3/examples/coroutines.py:209:18");
    }
}
