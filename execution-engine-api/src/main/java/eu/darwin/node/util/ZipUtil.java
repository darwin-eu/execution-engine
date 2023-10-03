package eu.darwin.node.util;

import eu.darwin.node.exceptions.ExecutionEngineExeception;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtil {

    private ZipUtil() {
    }

    public static byte[] zip(File directory) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<>();
        queue.push(directory);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(baos);
             ZipOutputStream zout = new ZipOutputStream(bos)) {
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        try (FileInputStream fis = new FileInputStream(kid)) {
                            zout.putNextEntry(new ZipEntry(name));
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = fis.read(bytes)) >= 0) {
                                zout.write(bytes, 0, length);
                            }
                        }
                    }
                }
            }
            zout.finish();
            zout.flush();
            return baos.toByteArray();
        }
    }

    public static void unzipFiles(File zipArchive, File destination) throws FileNotFoundException {
        if (destination != null && destination.exists()) {
            String destPath = destination.getAbsolutePath();
            try (ZipFile zipFile = new ZipFile(zipArchive)) {
                zipFile.extractAll(destPath);
            } catch (ZipException var4) {
                log.error(var4.getMessage(), var4);
            } catch (IOException e) {
                log.error(e.toString());
                throw new ExecutionEngineExeception("Failed to unzip files");
            }

        } else {
            throw new FileNotFoundException("Destination directory must exist");
        }
    }
}
