package eu.darwin.node.repo;


import eu.darwin.node.domain.LibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryItemRepo extends JpaRepository<LibraryItem, Long> {


}
