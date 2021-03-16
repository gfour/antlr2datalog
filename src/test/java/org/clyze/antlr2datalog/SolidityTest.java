package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class SolidityTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/solidity/test.sol";

    @Test
    public void testSolidity() {
        Main.main(new String[] { "-l", "solidity", "-i", SRC_FILE, "-g" });
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert metadataExist();
        assert functionDeclaration("FunctionDefinition@grammars-v4/solidity/test.sol@58-67", "f", "grammars-v4/solidity/test.sol:15:13");
        assert functionDefinition("FunctionDefinition@grammars-v4/solidity/test.sol@1604-1624", "fun", "grammars-v4/solidity/test.sol:368:13");
        assert functionArity("FunctionDefinition@grammars-v4/solidity/test.sol@1604-1624", "2");
        assert functionParameter("FunctionDefinition@grammars-v4/solidity/test.sol@1604-1624", "Identifier@grammars-v4/solidity/test.sol@1611-1611", "b", "1", "grammars-v4/solidity/test.sol:368:30");
    }
}
