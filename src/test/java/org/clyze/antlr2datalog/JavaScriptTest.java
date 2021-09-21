package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.jupiter.api.Test;

public class JavaScriptTest extends LangTest {

    @Test
    public void testJavaScript() throws IOException {
        Main.main(new String[] { "-l", "javascript", "-g",
                "-i", "grammars-v4/javascript/javascript/examples/Generators.js",
                "-i", "grammars-v4/javascript/javascript/examples/Classes.js"
        });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert functionDefinition("FunctionDeclaration@grammars-v4/javascript/javascript/examples/Generators.js@430-649", "fasync", "grammars-v4/javascript/javascript/examples/Generators.js:82:9");
        assert functionArity("FunctionDeclaration@grammars-v4/javascript/javascript/examples/Generators.js@430-649", "2");
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.fields.size() == 1;
        assert sm.functions.size() == 29;
        assert sm.sourceFiles.size() == 2;
        assert sm.types.size() == 16;
        assert sm.variables.size() == 59;
    }
}
