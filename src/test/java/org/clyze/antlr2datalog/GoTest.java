package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.Test;

public class GoTest extends LangTest {
    @Test
    public void testGo() throws IOException {
        Main.main(new String[] { "-l", "go", "-i", "src/test/resources/bit_cmd.go", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_Function_Area.csv")).exists());
        assert functionDefinition("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "HijackGitCommandOccurred", "src/test/resources/bit_cmd.go:134:5");
        assert functionArity("FunctionDecl@src/test/resources/bit_cmd.go@1074-1544", "3");
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.functions.size() == 9;
        assert sm.types.size() == 0;
    }
}
