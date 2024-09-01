package net.library.controller;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import net.library.exception.HttpCidSuccessResponse;
import net.library.exception.MdcUtils;
import net.library.object.Book;
import net.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.library.util.HttpUtil.BOOKS;

@ControllerAdvice
@RestController
@RequestMapping(BOOKS)
public class BookController {
private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private final BookService service;

    public BookController(final BookService service) {
        this.service = service;
    }

    @PostConstruct
    public void initBookList() {
        service.initBookList();
    }

    @GetMapping()
    public ResponseEntity<List<Book>> getBooks() {

        return ResponseEntity.ok(service.getBooks());
    }

    @PostMapping()
    public ResponseEntity<HttpCidSuccessResponse> addBook(@Valid @RequestBody Book book) {

        service.addBook(book);

        return ResponseEntity.ok(HttpCidSuccessResponse.of(MdcUtils.getCid()));

    }
}
