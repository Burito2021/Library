package net.library.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.library.service.validator.PasswordValidator;
import net.library.service.validator.PhoneNumberValidator;

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
