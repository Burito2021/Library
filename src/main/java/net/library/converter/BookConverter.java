package net.library.converter;

import net.library.model.entity.Book;
import net.library.model.request.BookRequest;

public class BookConverter {

    public static Book of(final BookRequest bookRequest) {
        var book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setDescription(bookRequest.getDescription());
        book.setPublisher(bookRequest.getPublisher());
        book.setEdition(bookRequest.getEdition());
        book.setPublicationYear(bookRequest.getPublication());

        return book;
    }
}