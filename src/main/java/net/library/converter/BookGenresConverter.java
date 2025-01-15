package net.library.converter;

import net.library.model.entity.BookGenre;
import net.library.model.request.BookGenreRequest;

import java.util.List;

public class BookGenresConverter {

    public static List<BookGenre> of(final BookGenreRequest bookGenreRequest) {
        return bookGenreRequest.getGenreId().stream().map(
                genreId -> {
                    BookGenre bookGenres = new BookGenre();
                    bookGenres.setBook_id(bookGenreRequest.getBookId());
                    bookGenres.setGenre_id(genreId);
                    return bookGenres;
                }).toList();
    }
}