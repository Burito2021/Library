package net.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.dto.BookGenreDto;
import net.library.model.dto.GenreDto;
import net.library.model.dto.Page;
import net.library.model.request.BookGenreRequest;
import net.library.model.request.GenreRequest;
import net.library.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static net.library.util.HttpUtil.GENRES;

@RequiredArgsConstructor
@RestController
@RequestMapping(GENRES)
public class GenreController {
    private final BookService service;

    @PostMapping
    public ResponseEntity<GenreDto> addGenre(@Valid @RequestBody GenreRequest genreRequest) {
        return ResponseEntity.status(201).body(service.addGenre(genreRequest));
    }

    @Operation(summary = "Get all available genres",
            description = "retrieve all available genres and their ids")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    }
    )
    @GetMapping
    public Page<GenreDto> getAllGenre(
            @PageableDefault(size = 10, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var response = service.getGenres(pageable);
        return new Page<>(response.getSize(), response.getNumber(), response.getTotalElements(),
                response.get().collect(Collectors.toList()));
    }

    @Operation(summary = "Add genre for a book",
            description = "save genre for a specific book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully saved"),
            @ApiResponse(responseCode = "400", description = "Mandatory param missing")
    }
    )
    @PostMapping("/book")
    public ResponseEntity<List<BookGenreDto>> addBookGenres(@Valid @RequestBody BookGenreRequest bookRequest) {
        return ResponseEntity.status(201).body(service.addBookGenres(bookRequest));
    }

    @Operation(summary = "Get all the books and their genres",
            description = "retrieve all the genres available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "retrieved all the books and their genres")
    }
    )
    @GetMapping("/book")
    public Page<BookGenreDto> getAllBookGenre(
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var response = service.getBookGenre(pageable);
        return new Page<>(response.getSize(), response.getNumber(), response.getTotalElements(),
                response.get().collect(Collectors.toList()));
    }
}
