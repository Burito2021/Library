package net.library.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AddBookResponse {

    private UUID id;

    private String title;

    private String author;

    private String description;

    private String publisher;

    private String edition;

    private Integer publication;
}
