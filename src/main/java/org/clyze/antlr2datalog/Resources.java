package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

/** This class gathers functionality for bundled resources. */
public class Resources {
    /**
     * Extract a bundled archive to a temporary directory.
     * @param loader       the class loader to use
     * @param tmpName      the prefix of the temporary directory to create
     * @param archiveName  the name of the bundled archive
     * @return             the temporary directory (to be deleted on VM exit)
     * @throws IOException on extraction error
     */
    public static File extractResourceArchive(ClassLoader loader, String tmpName,
                                              String archiveName) throws IOException {
        InputStream is = loader.getResourceAsStream(archiveName);
        if (is == null)
            throw new IOException("Error: could not find directory '" + archiveName + "'");
        return extractInputStream(is, tmpName);
    }

    public static File extractInputStream(InputStream is, String tmpName) throws IOException {
        File tmpDir = Files.createTempDirectory(tmpName).toFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(tmpDir)));
        ZipUtil.unpack(is, tmpDir);
        return tmpDir;
    }

    /**
     * Extract a bundled file.
     * @param loader       the class loader to use
     * @param filePath     the resource path
     * @return             the local filesystem path of the extracted file
     * @throws IOException on I/O error when handling the resource
     */
    public static String extractResourceFile(ClassLoader loader, String filePath) throws IOException {
        InputStream is = loader.getResourceAsStream(filePath);
        if (is == null)
            throw new RuntimeException("Cannot find resource: " + filePath);
        int slashPos = filePath.lastIndexOf(File.separator);
        String tmpSuffix = slashPos >= 0 ? filePath.substring(slashPos + File.separator.length()) : ".tmp";
        File tmpFile = File.createTempFile("tmp", tmpSuffix);
        tmpFile.deleteOnExit();
        org.apache.commons.io.FileUtils.copyInputStreamToFile(is, tmpFile);
        return tmpFile.getCanonicalPath();
    }
}
