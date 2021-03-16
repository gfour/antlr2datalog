package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class Cobol85Test extends LangTest {
    public static final String SRC_FILE = "src/test/resources/InputSORT.cbl";

    @Test
    public void testCobol85() {
        Main.main(new String[] { "-l", "cobol85", "-i", SRC_FILE });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_StringConstant.csv")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
    }
}
