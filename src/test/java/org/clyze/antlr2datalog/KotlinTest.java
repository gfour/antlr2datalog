package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class KotlinTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/kotlin/kotlin-formal/examples/Test.kt";

    @Test
    public void testKotlin() {
        Main.main(new String[] { "-l", "kotlin", "-i", SRC_FILE });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
        assert functionDeclaration("FunctionDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@6859-6869", "property", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:832:8");
        assert functionDefinition("FunctionDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@4298-4353", "main", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:559:4");
        assert functionArity("FunctionDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@4298-4353", "1");
        assert variableDeclaration("SimpleIdentifier@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@4302-4302", "args", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:559:9");
        assert variableDeclaration("VariableDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@2391-2391", "r", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:328:8");
        assert variableDeclaration("VariableDeclaration@grammars-v4/kotlin/kotlin-formal/examples/Test.kt@2425-2425", "f", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt:333:11");
    }
}
