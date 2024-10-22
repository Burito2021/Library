package net.library.converter;

import net.library.model.entity.Genre;
import net.library.model.request.GenreRequest;

public class GenreConverter {

    public static Genre of(final GenreRequest genreRequest) {
        var genre = new Genre();
        genre.setName(genreRequest.getName());

        return genre;
    }
}