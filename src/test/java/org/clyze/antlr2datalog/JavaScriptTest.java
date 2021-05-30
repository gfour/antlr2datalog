package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class JavaScriptTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/javascript/javascript/examples/Generators.js";

    @Test
    public void testJavaScript() {
        Main.main(new String[] { "-l", "javascript", "-i", SRC_FILE });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert functionDefinition("FunctionDeclaration@grammars-v4/javascript/javascript/examples/Generators.js@430-649", "fasync", "grammars-v4/javascript/javascript/examples/Generators.js:82:9");
        assert functionArity("FunctionDeclaration@grammars-v4/javascript/javascript/examples/Generators.js@430-649", "2");
    }
}
