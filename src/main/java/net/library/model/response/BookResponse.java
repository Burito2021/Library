package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.library.model.dto.BookGenreDto;
import net.library.model.dto.BookItemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookResponse {

    @JsonProperty("bookId")
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("description")
    private String description;

    @JsonProperty("edition")
    private String edition;

    @JsonProperty("publicationyYear")
    private Integer publicationYear;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("deletedAt")
    private LocalDateTime deletedAt;

    @JsonProperty("bookItems")
    private List<BookItemDto> bookItems;

    @JsonProperty("bookGenres")
    private List<BookGenreDto> genres;
}
