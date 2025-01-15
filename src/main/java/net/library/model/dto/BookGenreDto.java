package net.library.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BookGenreDto {

    private UUID bookId;

    private UUID genreId;
}