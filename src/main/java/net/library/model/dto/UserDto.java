package net.library.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    private String username;

    private String name;

    private String surname;

    private String email;

    private String phoneNumber;

    private String address;
}
