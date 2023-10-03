package eu.darwin.node.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStore {

    private UserInfo userInfo;

    public void clear() {
        this.userInfo = null;
    }

}
