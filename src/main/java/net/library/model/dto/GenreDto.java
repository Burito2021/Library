package net.library.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class GenreDto {

    @JsonProperty("id")
    private UUID genreId;

    @JsonProperty("genreName")
    private String genreName;
}
