package eu.darwin.node.controller;

import eu.darwin.node.domain.DataSource;
import eu.darwin.node.dto.DataSourceDTO;
import eu.darwin.node.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping(path = "/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService service;

    @PostMapping
    public void create(@RequestBody DataSource request) {
        service.save(request);
    }

    @PutMapping
    public void update(@RequestBody DataSource request) {
        service.update(request);
    }

    @GetMapping
    public List<DataSourceDTO> list() {
        return service.findAll().stream().map(DataSourceDTO::fromEntity).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
