package net.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.dto.BookDto;
import net.library.model.dto.Page;
import net.library.model.request.BookRequest;
import net.library.service.BookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.library.util.HttpUtil.BOOKS;

@RequiredArgsConstructor
@RestController
@RequestMapping(BOOKS)
public class BookController {
    private final BookService service;

    @PostMapping
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody BookRequest bookRequest) {

        return ResponseEntity.status(201).body(service.addBook(bookRequest));
    }

    @GetMapping
    public Page<BookDto> getAllBooks(
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        var sortBy = params.get("sortBy");
        var sortFields = sortBy != null && !sortBy.isEmpty()
                ? List.of(sortBy.split(","))
                : List.of("author", "createdAt");

        var direction = Sort.Direction.fromOptionalString(params.get("order")).orElse(Sort.Direction.DESC);
        var sort = Sort.by(sortFields.stream().map(field -> new Sort.Order(direction, field)).toList());

        var books = service.getAllBooks(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));

        return new Page<>(books.getSize(), books.getNumber(), books.getTotalElements(),
                books.get().collect(Collectors.toList()));
    }
}
