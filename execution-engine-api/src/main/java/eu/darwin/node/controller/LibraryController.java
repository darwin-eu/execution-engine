package eu.darwin.node.controller;

import eu.darwin.node.domain.LibraryItem;
import eu.darwin.node.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping(path = "/library")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class LibraryController {

    private final LibraryService service;


    @GetMapping(path = "/items")
    public List<LibraryItem> list() {
        return service.findAll();
    }

    @PostMapping(path = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void create(@RequestPart("file") List<MultipartFile> files, @RequestPart("item") LibraryItem item) {
        if (files.size() != 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Upload one single zip file");
        }
        service.save(files.get(0), item);
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
