package eu.darwin.node.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfo {

    @JsonProperty("sub")
    private String sub;
    @JsonProperty("name") // Azure AD
    @JsonAlias("preferred_username") // Keycloak
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("exp")
    private long expiration;

}
