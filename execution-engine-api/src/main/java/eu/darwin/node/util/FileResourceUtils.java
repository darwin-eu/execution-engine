package eu.darwin.node.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileResourceUtils {

    private FileResourceUtils() {
    }

    public static void unpack(TarArchiveInputStream tis, File outputDir) throws IOException {
        for (TarArchiveEntry entry = tis.getNextTarEntry(); entry != null; ) {
            unpackEntries(tis, entry, outputDir);
            entry = tis.getNextTarEntry();
        }
    }

    private static void unpackEntries(TarArchiveInputStream tis, TarArchiveEntry entry, File outputDir) throws IOException {
        if (entry.isDirectory()) {
            File subDir = new File(outputDir, entry.getName());
            if (!subDir.mkdirs() && !subDir.isDirectory()) {
                throw new IOException("Mkdirs failed to create tar internal dir "
                        + outputDir);
            }
            for (TarArchiveEntry e : entry.getDirectoryEntries()) {
                unpackEntries(tis, e, subDir);
            }
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new IOException("Mkdirs failed to create tar internal dir " + outputDir);
        }

        int count;
        byte[] data = new byte[2048];
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            while ((count = tis.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }
            outputStream.flush();
        }
    }
}
