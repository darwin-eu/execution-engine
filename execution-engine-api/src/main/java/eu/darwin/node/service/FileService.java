package eu.darwin.node.service;

import eu.darwin.node.exceptions.ExecutionEngineExeception;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.darwin.node.service.AnalysisService.ZIP_FILE_NAME;

@Service
@Slf4j
public class FileService {

    private static final String ARCHIVE_SUBDIR = "archive";

    @Value("${files.store.path:}")
    private String filesStorePath;


    public File saveMultipartToZip(File dir, MultipartFile archive) throws IOException {
        File zipDir = makeZipDir(dir);
        File archiveFile = new File(zipDir, ZIP_FILE_NAME);
        archive.transferTo(archiveFile.toPath());
        return archiveFile;
    }

    public String checksum(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ExecutionEngineExeception("Failed to create checksum");
        }
    }

    public File makeZipDir(File dir) throws IOException {
        System.out.println(dir.getAbsolutePath());
        final File zipDir = Paths.get(dir.getPath(), ARCHIVE_SUBDIR).toFile();
        FileUtils.forceMkdir(zipDir);
        return zipDir;
    }


    public File createDir() {
        String baseName = String.valueOf(System.currentTimeMillis());
        Path uniquePath = Paths.get(filesStorePath, baseName);
        try {
            return Files.createDirectories(uniquePath).toFile();
        } catch (IOException e) {
            log.error(e.toString());
            throw new ExecutionEngineExeception("Failed to create a directory");
        }
    }


}
