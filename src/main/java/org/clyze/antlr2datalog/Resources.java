package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

public class Resources {
    public static File extractResourceDir(ClassLoader loader, String dirName, boolean debug) throws IOException {
        InputStream is = loader.getResourceAsStream(dirName);
        if (is == null)
            throw new IOException("Error: could not find directory '" + dirName + "'");
        File tmpDir = Files.createTempDirectory(dirName).toFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(tmpDir)));
        ZipUtil.unpack(is, tmpDir);
        return tmpDir;
    }

    public static String extractResourceFile(ClassLoader loader, String filePath) throws IOException {
        InputStream is = loader.getResourceAsStream(filePath);
        int slashPos = filePath.lastIndexOf(File.separator);
        String tmpSuffix = slashPos >= 0 ? filePath.substring(slashPos + File.separator.length()) : ".tmp";
        File tmpFile = File.createTempFile("tmp", tmpSuffix);
        tmpFile.deleteOnExit();
        org.apache.commons.io.FileUtils.copyInputStreamToFile(is, tmpFile);
        return tmpFile.getCanonicalPath();
    }
}
