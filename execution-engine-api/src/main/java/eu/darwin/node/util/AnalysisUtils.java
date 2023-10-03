package eu.darwin.node.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class AnalysisUtils {

    private static final String VISITOR_ACCESS_ERROR = "Access error when access to file '{}'. Skipped";

    private AnalysisUtils() {
    }

    public static List<File> getDirectoryItems(File parentDir, Function<Path, Optional<File>> func) {

        if (!parentDir.isDirectory()) {
            throw new IllegalArgumentException();
        }
        List<File> result = new ArrayList<>();
        try {
            Files.walkFileTree(parentDir.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {

                    func.apply(path).ifPresent(result::add);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.info(VISITOR_ACCESS_ERROR, file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        result.sort(Comparator.comparing(File::getName));
        return result;
    }

    public static List<File> getDirectoryItems(File parentDir) {

        return getDirectoryItems(parentDir, p -> Optional.of(p.toFile()));
    }

}
