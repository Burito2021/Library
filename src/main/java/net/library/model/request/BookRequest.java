package net.library.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters.")
    private String title;

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 50, message = "Author must be between 1 and 50 characters.")
    private String author;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 100, message = "Description must not exceed 100 characters.")
    private String publisher;

    @Size(max = 30, message = "Description must not exceed 30 characters.")
    private String edition;

    private Integer publication;
}
