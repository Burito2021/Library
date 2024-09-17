package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.library.model.dto.UserDto;

import java.util.List;

public class UserResponse {

    @JsonProperty("users")
    private List<UserDto> users;

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}
