package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.clyze.persistent.metadata.JSONUtil;
import org.clyze.persistent.metadata.SourceMetadata;
import org.junit.Before;
import org.junit.Test;
//import static org.junit.Assert.*;

public class LangTest {
    @Before
    public void deleteWorkspace() {
        FileUtils.deleteQuietly(new File(Main.DEFAULT_WORKSPACE));
    }

    protected boolean metadataExist() {
        return getMetadataFile().exists();
    }

    protected static File getDatabase() {
        return new File(Main.DEFAULT_WORKSPACE, "database");
    }

    protected static File getMetadataFile() {
        return new File(getDatabase(), MetadataGenerator.OUTPUT_FILE);
    }

    protected static SourceMetadata getSourceMetadata() throws IOException {
        return SourceMetadata.fromMap(JSONUtil.toMap(getMetadataFile().toPath()));
    }

    protected static boolean relationTuple(String relName, java.util.function.Function<String[], Boolean> test) {
        File csv = new File(getDatabase(), relName);
        try {
            return Files.lines(csv.toPath()).map((String line) -> line.split("\t")).anyMatch(test::apply);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean functionArity(String funcId, String arity) {
        return findTuple("BASE_Function_Arity.csv", funcId, arity);
    }

    protected boolean functionDefinition(String funcId, String name, String loc) {
        return findTuple("BASE_FunctionDefinition.csv", funcId, name, loc);
    }

    protected boolean functionDeclaration(String funcId, String name, String loc) {
        return findTuple("BASE_FunctionDeclaration.csv", funcId, name, loc);
    }

    protected boolean functionParameter(String funcId, String paramId, String name, String idx, String loc) {
        return findTuple("BASE_FunctionParameter.csv", funcId, paramId, name, idx, loc);
    }

    protected boolean variableDeclaration(String id, String name, String loc) {
        return findTuple("BASE_VariableDeclaration.csv", id, name, loc);
    }

    protected boolean findTuple(String relName, String... values) {
        return relationTuple(relName, ((String[] parts) -> {
            if (parts.length != values.length) {
                System.err.println("ERROR: mismatched arity in findTuple()");
                return false;
            }
            for (int i = 0; i < parts.length; i++)
                if (!parts[i].equals(values[i]))
                    return false;
            return true;
        }));
    }
}
