package eu.darwin.node.repo;

import eu.darwin.node.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepo extends JpaRepository<Log, Long> {

}
