package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.Test;

public class CTest extends LangTest {
    @Test
    public void testC() throws IOException {
        Main.main(new String[] { "-l", "c", "-i", "grammars-v4/c/examples/FuncForwardDeclaration.c", "-i", "grammars-v4/c/examples/ll.c", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert functionDefinition("FunctionDefinition@grammars-v4/c/examples/FuncForwardDeclaration.c@33-59", "f", "grammars-v4/c/examples/FuncForwardDeclaration.c:12:4");
        assert functionDeclaration("ExternalDeclaration@grammars-v4/c/examples/FuncForwardDeclaration.c@0-5", "aX", "grammars-v4/c/examples/FuncForwardDeclaration.c:6:5");
        assert functionArity("FunctionDefinition@grammars-v4/c/examples/FuncForwardDeclaration.c@33-59", "2");
        assert variableDeclaration("TerminalNode@grammars-v4/c/examples/FuncForwardDeclaration.c@37-37", "arg1", "grammars-v4/c/examples/FuncForwardDeclaration.c:12:10");
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.functions.size() == 2;
        assert sm.types.size() == 1;
    }
}
