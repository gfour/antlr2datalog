package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;

/**
 * This test ensures that the generated facts/logic are always the same, so
 * that (a) the analysis is reproducible and later runs refer to the same
 * elements and (b) Datalog text does not change between runs.
 */
public class ReproducibleAnalyses {
    private static File getTempWorkspace(int n) {
        return new File("build", Main.DEFAULT_WORKSPACE + n);
    }

    @Before
    public void deleteWorkspaces() {
        FileUtils.deleteQuietly(getTempWorkspace(1));
        FileUtils.deleteQuietly(getTempWorkspace(2));
    }

    @Test public void test() throws IOException {
        File wdir1 = getTempWorkspace(1);
        File wdir2 = getTempWorkspace(2);
        for (File wDir : new File[] { wdir1, wdir2 })
            Main.main(new String[] { "-l", "rust",
                    "-i", "grammars-v4/rust/examples/deno_core_runtime.rs",
                    "-w", wDir.getCanonicalPath() });

        assert treesAreTheSame(wdir1, wdir2);
    }

    private static boolean treesAreTheSame(File file1, File file2) throws IOException {
        // Runtime statistics relations may differ but that does not affect reproducibility.
        if (file1.getName().equals(Driver.STATS_METRICS_FILE) && file2.getName().equals(Driver.STATS_METRICS_FILE)) {
            System.out.println("Ignoring relation: " + Driver.STATS_METRICS_FILE);
            return true;
        }
        System.out.println("Checking " + file1 + " against " + file2);
        if (file1.exists() && file2.exists()) {
            if (file1.isDirectory() && file2.isDirectory()) {
                File[] files1 = file1.listFiles();
                File[] files2 = file2.listFiles();
                if (files1 == null && files2 == null)
                    return true;
                if (files1 != null && files2 != null && files1.length == files2.length) {
                    boolean result = true;
                    for (File f1 : files1) {
                        File f2 = new File(file2, f1.getName());
                        result &= treesAreTheSame(f1, f2);
                    }
                    return result;
                }
                System.err.println("ERROR: directory size differs: " + file1 + ", " + file2);
                return false;
            } else if (file1.isFile() && file2.isFile()) {
                boolean result = FileUtils.readFileToString(file1).equals(FileUtils.readFileToString(file2));
                if (!result)
                    System.err.println("ERROR: files differ: " + file1 + ", " + file2);
                return result;
            } else {
                System.err.println("ERROR: paths should be both files/directories: " + file1 + ", " + file2);
                return false;
            }
        } else {
            System.err.println("ERROR: could not find both files: " + file1 + ", " + file2);
            return false;
        }
    }
}
