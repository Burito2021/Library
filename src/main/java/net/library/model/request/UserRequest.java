package net.library.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.library.service.validator.password.PasswordValidator;
import net.library.service.validator.phonenumber.PhoneNumberValidator;

@Data
@AllArgsConstructor
public class UserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    @Email
    private String email;

    @PhoneNumberValidator
    private String phoneNumber;

    private String address;

    @PasswordValidator
    private String password;
}
