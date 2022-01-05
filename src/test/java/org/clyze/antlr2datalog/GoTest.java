package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.jupiter.api.Test;

public class GoTest extends LangTest {
    public static final String SRC_FILE = "src/test/resources/bit_cmd.go";
    @Test
    public void testGo() throws IOException {
        Main.main(new String[] { "-l", "go", "-i", SRC_FILE, "-i", "grammars-v4/golang/examples/structs.go", "-g", "-p" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_Function_Area.csv")).exists());
        assert functionDefinition("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "HijackGitCommandOccurred", ":src/test/resources/bit_cmd.go:134:5");
        assert functionArity("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "3");
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.fields.size() == 14;
        assert sm.functions.size() == 11;
        assert sm.sourceFiles.size() == 2;
        assert sm.types.size() == 10;
        assert sm.variables.size() == 37;
    }
}
