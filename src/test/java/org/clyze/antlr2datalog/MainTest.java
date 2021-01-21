package org.clyze.antlr2datalog;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
//import static org.junit.Assert.*;

public class MainTest {
    private static void deleteWorkspace() {
        FileUtils.deleteQuietly(new File(Main.DEFAULT_WORKSPACE));
    }

    @Test public void modePython3() {
        deleteWorkspace();
        String[] args = new String[] { "-l", "python3", "-i", "grammars-v4/python/python3/examples/coroutines.py", "--compile" };
        Main.main(args);
        assert((new File(Main.DEFAULT_WORKSPACE, "database/Function.csv")).exists());
    }

    @Test public void modeKotlin() {
        deleteWorkspace();
        String[] args = new String[] { "-l", "kotlin", "-i", "grammars-v4/kotlin/kotlin-formal/examples/Test.kt" };
        Main.main(args);
        assert((new File(Main.DEFAULT_WORKSPACE, "database/UsedVariable.csv")).exists());
    }

    @Test public void modeCobol85() {
        deleteWorkspace();
        String[] args = new String[] { "-l", "cobol85", "-i", "grammars-v4/cobol85/examples/example1.txt" };
        Main.main(args);
        assert((new File(Main.DEFAULT_WORKSPACE, "database/StringConstant.csv")).exists());
    }
}
