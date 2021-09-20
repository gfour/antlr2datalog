package org.clyze.antlr2datalog;

import java.io.File;
import org.junit.jupiter.api.Test;

public class LuaTest extends LangTest {
    public static final String SRC_FILE = "grammars-v4/lua/examples/factorial.lua";

    @Test
    public void testLua() {
        Main.main(new String[] { "-l", "lua", "-i", SRC_FILE });
        assert((new File(Main.DEFAULT_WORKSPACE, "facts/DB_LUA_Var__NAME.facts")).exists());
        assert((new File(Main.DEFAULT_WORKSPACE, "database/BASE_FunctionDefinition.csv")).exists());
        assert functionDefinition("Funcbody@grammars-v4/lua/examples/factorial.lua@3-24", "fact", "grammars-v4/lua/examples/factorial.lua:2:13");
        assert functionArity("Funcbody@grammars-v4/lua/examples/factorial.lua@3-24", "1");
        assert variableDeclaration("TerminalNode@grammars-v4/lua/examples/factorial.lua@4-4", "n", "grammars-v4/lua/examples/factorial.lua:2:19");
        assert variableDeclaration("TerminalNode@grammars-v4/lua/examples/factorial.lua@29-29", "a", "grammars-v4/lua/examples/factorial.lua:11:4");
    }
}
