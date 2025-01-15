package net.library.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "title", example = "White fang")
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters.")
    private String title;

    @Schema(name = "author", example = "Jack London")
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 50, message = "Author must be between 1 and 50 characters.")
    private String author;

    @Schema(name = "description", example = "A long lasting novel")
    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    @Schema(name = "publisher", example = "London house")
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 100, message = "Description must not exceed 100 characters.")
    private String publisher;

    @Schema(name = "edition", example = "3th")
    @Size(max = 30, message = "Description must not exceed 30 characters.")
    private String edition;

    @Schema(name = "publication", example = "2011")
    private Integer publication;
}
