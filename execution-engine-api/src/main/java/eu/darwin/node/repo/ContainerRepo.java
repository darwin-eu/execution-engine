package eu.darwin.node.repo;


import eu.darwin.node.domain.Analysis;
import eu.darwin.node.domain.Container;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContainerRepo extends JpaRepository<Container, Long> {

    Optional<Container> findFirstByAnalysis(Analysis analysis);

    Optional<Container> findFirstByContainerId(String containerId);


}
