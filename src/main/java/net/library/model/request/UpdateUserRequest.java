package net.library.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.library.service.validator.password.PasswordValidator;

@Data
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String address;
    @PasswordValidator
    private String password;
}
