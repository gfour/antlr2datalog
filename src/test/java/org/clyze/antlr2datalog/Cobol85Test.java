package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.Test;

public class Cobol85Test extends LangTest {
    @Test
    public void testCobol85() {
        Main.main(new String[] { "-l", "cobol85", "-i", "src/test/resources/InputSORT.cbl" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_StringConstant.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }
}
