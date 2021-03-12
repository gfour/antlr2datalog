package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.Test;

public class SolidityTest extends LangTest {
    @Test
    public void testSolidity() {
        Main.main(new String[] { "-l", "solidity", "-i", "./grammars-v4/solidity/test.sol", "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert metadataExist();
    }
}
