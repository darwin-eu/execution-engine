package eu.darwin.node.repo;

import eu.darwin.node.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findFirstByOidcId(String oidcId);

}
