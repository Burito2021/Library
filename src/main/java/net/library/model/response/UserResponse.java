package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.library.model.entity.User;

import java.util.List;

public class UserResponse {

    @JsonProperty("x-Correlation-id")
    private String xCorrelationId;

    @JsonProperty("users")
    private List<User> users;

    public UserResponse(String xCorrelationId, List<User> users){
        this.xCorrelationId =xCorrelationId;
        this.users =users;
    }

    public String getxCorrelationId() {
        return xCorrelationId;
    }

    public void setxCorrelationId(String xCorrelationId) {
        this.xCorrelationId = xCorrelationId;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
