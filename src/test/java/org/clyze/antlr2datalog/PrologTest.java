package org.clyze.antlr2datalog;

import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PrologTest extends LangTest {
    public static final String SRC_FILE = "src/test/resources/nqueens.pl";

    @Test
    public void testProlog() throws IOException {
        Main.main(new String[] { "-l", "prolog", "-i", SRC_FILE, "-g" });
        assert metadataExist();
        SourceMetadata sm = getSourceMetadata();
        assert sm.functions.size() > 0;
    }
}
