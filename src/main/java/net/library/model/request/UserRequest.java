package net.library.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.library.service.validator.PhoneNumberValidator;

@Data
@AllArgsConstructor
public class UserRequest {

    @NotNull
    @NotEmpty
    @NotBlank
    private String username;

    @NotNull
    @NotEmpty
    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    @NotBlank
    private String surname;

    @NotNull
    @NotEmpty
    @NotBlank
    @Email
    private String email;

    @PhoneNumberValidator
    private String phoneNumber;

    private String address;
}
