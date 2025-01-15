package net.library.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BookGenreRequest {

    @NotNull
    private UUID bookId;
    @NotNull
    private List<UUID> genreId;
}
