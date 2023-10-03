package eu.darwin.node.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String oidcId;
    private String username;

    public User(String oidcId, String username) {
        this.oidcId = oidcId;
        this.username = username;
    }

    public static User anonymous() {
        return new User("anonymous user", "anonymous user");
    }

}
