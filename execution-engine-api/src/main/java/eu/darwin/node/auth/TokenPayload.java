package eu.darwin.node.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenPayload {

    @JsonProperty("name")
    private String username;
    @JsonProperty("exp")
    private long expiration;
    @JsonProperty("iss")
    private String issuer;
}
