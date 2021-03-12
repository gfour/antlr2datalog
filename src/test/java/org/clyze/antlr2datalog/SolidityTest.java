package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.Test;

public class SolidityTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/solidity/test.sol";

    @Test
    public void testSolidity() {
        Main.main(new String[] { "-l", "solidity", "-i", SRC_FILE, "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert metadataExist();
    }
}
