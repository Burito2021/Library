package net.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.request.BookRequest;
import net.library.model.response.AddBookResponse;
import net.library.model.response.BookResponse;
import net.library.model.response.Page;
import net.library.service.BookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.library.util.HttpUtil.BOOKS;

@RequiredArgsConstructor
@RestController
@RequestMapping(BOOKS)
public class BookController {
    private final BookService service;

    @Operation(summary = "Add  a book entity to database", description = "saves book to database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "400", description = "when mandatory param is missing")
    }
    )
    @PostMapping
    public ResponseEntity<AddBookResponse> addBook(@Valid @RequestBody BookRequest bookRequest) {
        return ResponseEntity.status(201).body(service.addBook(bookRequest));
    }

    @GetMapping("/all")
    public Page<BookResponse> getAll(
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        var sortBy = params.get("sortBy");
        var sortFields = sortBy != null && !sortBy.isEmpty()
                ? List.of(sortBy.split(","))
                : List.of("author", "createdAt");

        var direction = Sort.Direction.fromOptionalString(params.get("order")).orElse(Sort.Direction.DESC);
        var sort = Sort.by(sortFields.stream().map(field -> new Sort.Order(direction, field)).toList());

        final var pages = service.getAllEntities(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        return new Page<>(pages.getSize(),pages.getNumber(),pages.getTotalElements(),
                pages.get().collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }
}
