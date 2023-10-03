package eu.darwin.node.service;

import eu.darwin.node.domain.DataSource;
import eu.darwin.node.repo.DataSourceRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSourceService {

    private final DataSourceRepo repo;

    public DataSource findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<DataSource> findAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void save(DataSource dataSource) {
        repo.save(dataSource);
    }

    public void update(DataSource request) {
        if (request.password() == null || request.password().isBlank()) {
            // pwd was not updated just persist the whole thing, need to reset it so not to nullify on save
            var currentpwd = findById(request.id()).password();
            request.password(currentpwd);
        }
        repo.save(request);
    }
}
