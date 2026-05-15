package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MostBorrowedBookResponse {

    @JsonProperty("bookId")
    private UUID bookId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("publicationYear")
    private Integer publicationYear;

    @JsonProperty("borrowCount")
    private long borrowCount;
}
