package net.library.model.mapper;

import net.library.model.entity.Book;
import net.library.model.response.AddBookResponse;

import java.util.List;
import java.util.stream.Collectors;

public class BookMapper {

    public static AddBookResponse toDto(Book book) {
        return new AddBookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPublisher(),
                book.getEdition(), book.getPublicationYear());
    }

    public static List<AddBookResponse> toDto(List<Book> books) {
        return books.stream().map(BookMapper::toDto).collect(Collectors.toList());
    }
}
