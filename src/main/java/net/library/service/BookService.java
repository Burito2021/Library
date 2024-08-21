package net.library.service;

import net.library.exception.MdcUtils;
import net.library.object.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookService.class);

    private final List<Book> books = new ArrayList<>();

    public void initBookList() {
        var book1 = new Book("book One", "Author One");
        book1.setBook("700e3362-1c37-4c7e-9fb0-6e246cb54f73");
        var book2 = new Book("book Two", "Author Two");
        book2.setBook("700e3362-1c37-4c7e-9fb0-6e246cb54f72");
        var book3 = new Book("book Three", "Author Three");
        book3.setBook("700e3362-1c37-4c7e-9fb0-6e246cb54f71");
        books.add(book1);
        books.add(book2);
        books.add(book3);
    }

    public List<Book> getBooks() {
        LOGGER.debug("List of books returned: {} ",books.stream().toList());
        return books;
    }

    public void deleteAllBooks() {
        books.clear();
    }

    public void addBook(final Book book) {
        var bookId = MdcUtils.getCid();
        book.setBook(bookId);

        books.add(book);
        LOGGER.info("Book added with id:{}",bookId);
    }
}
