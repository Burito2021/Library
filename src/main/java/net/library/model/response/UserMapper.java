package net.library.model.response;

import net.library.model.dto.UserDto;
import net.library.model.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getEmail(), user.getPhoneNumber(), user.getAddress());
    }

    public static List<UserDto> toDto(List<User> users) {
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }
}
