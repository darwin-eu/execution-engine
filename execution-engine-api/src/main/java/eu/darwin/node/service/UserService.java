package eu.darwin.node.service;


import eu.darwin.node.auth.UserInfo;
import eu.darwin.node.auth.UserStore;
import eu.darwin.node.domain.User;
import eu.darwin.node.repo.UserRepo;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j

public class UserService {

    private final UserRepo repo;
    private final UserStore userStore;

    public UserService(UserRepo repo, UserStore userStore) {
        this.repo = repo;
        this.userStore = userStore;
    }

    public User current() {
        UserInfo userInfo = userStore.userInfo();
        if (userInfo == null) {
            userInfo = new UserInfo()
                    .name("anonymous user")
                    .sub("anonymous user");
        }
        final var ui = userInfo;
        var current = repo.findFirstByOidcId(userInfo.sub()).orElseGet(() -> save(ui));
        if (current.username() == null || !current.username().equals(userInfo.name())) {
            current.username(userInfo.name());
            save(current);
        }
        return current;
    }

    @Synchronized
    private User save(UserInfo u) {
        var user = new User(u.sub(), u.name());
        log.info("Creating new user {}", user);
        return save(user);
    }

    @Synchronized
    private User save(User user) {
        log.info("Saving user {}", user);
        return repo.saveAndFlush(user);
    }


    public List<User> all() {
        return repo.findAll();
    }
}
