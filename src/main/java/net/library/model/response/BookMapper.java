package net.library.model.response;

import net.library.model.dto.BookDto;
import net.library.model.entity.Book;

import java.util.List;
import java.util.stream.Collectors;

public class BookMapper {

    public static BookDto toDto(Book book) {
//   BookDto.builder().id()
        return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPublisher(),
                book.getEdition(), book.getPublicationYear());
    }

    public static List<BookDto> toDto(List<Book> books) {
        return books.stream().map(BookMapper::toDto).collect(Collectors.toList());
    }
}
