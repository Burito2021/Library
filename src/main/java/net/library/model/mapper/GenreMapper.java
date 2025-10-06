package net.library.model.mapper;

import net.library.model.dto.GenreDto;
import net.library.model.entity.Genre;

import java.util.List;
import java.util.stream.Collectors;

public class GenreMapper {

    public static GenreDto toDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }

    public static List<GenreDto> toDto(List<Genre> genre) {
        return genre.stream().map(GenreMapper::toDto).collect(Collectors.toList());
    }
}
