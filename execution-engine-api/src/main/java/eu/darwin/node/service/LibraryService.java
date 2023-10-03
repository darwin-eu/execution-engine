package eu.darwin.node.service;

import eu.darwin.node.domain.LibraryItem;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import eu.darwin.node.repo.LibraryItemRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final LibraryItemRepo repo;
    private final FileService fileService;


    public List<LibraryItem> findAll() {
        return repo.findAll();
    }

    public void save(MultipartFile multipartFile, LibraryItem item) {
        try {
            var folder = fileService.createDir();
            var zip = fileService.saveMultipartToZip(folder, multipartFile);
            item.link(zip.getAbsolutePath());
            repo.save(item);
        } catch (IOException e) {
            log.error(e.toString());
            throw new ExecutionEngineExeception("Failed to save library item");
        }
    }

    public LibraryItem byId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Library item with id: [" + id + "] not found"));
    }

    public void delete(Long id) {
        var item = byId(id);
        var path = Path.of(item.link());
        try {
            Files.delete(path);
            repo.delete(item);
        } catch (IOException e) {
            throw new ExecutionEngineExeception("Failed to delete library item with id:" + id);
        }
    }


}
