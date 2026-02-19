package net.library.model.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    //least 3
    private String username;
    @NotBlank
    //least 3
    private String password;
    @NotBlank
    //15 symbols
    private String fingerprint;
}
