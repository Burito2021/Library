package net.library.controller;

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

    @GetMapping
    public Page<GenreDto> getAllGenre(
            @PageableDefault(size = 10, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var response = service.getGenres(pageable);
        return new Page<>(response.getSize(), response.getNumber(), response.getTotalElements(),
                response.get().collect(Collectors.toList()));
    }

    @PostMapping("/book")
    public ResponseEntity<List<BookGenreDto>> addBookGenres(@Valid @RequestBody BookGenreRequest bookRequest) {
        return ResponseEntity.status(201).body(service.addBookGenres(bookRequest));
    }

    @GetMapping("/book")
    public Page<BookGenreDto> getAllBookGenre(
            @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var response = service.getBookGenre(pageable);
        return new Page<>(response.getSize(), response.getNumber(), response.getTotalElements(),
                response.get().collect(Collectors.toList()));
    }
}
