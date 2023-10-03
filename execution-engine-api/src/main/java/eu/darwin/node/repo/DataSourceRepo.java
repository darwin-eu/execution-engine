package eu.darwin.node.repo;

import eu.darwin.node.domain.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepo extends JpaRepository<DataSource, Long> {
}
