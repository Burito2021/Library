package net.library.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private String username;

    private String name;

    private String surname;

    private String email;

    private String phoneNumber;

    private String address;
}
