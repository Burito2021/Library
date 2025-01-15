package net.library.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreRequest {

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters.")
    private String name;
}
