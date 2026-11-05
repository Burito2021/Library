package net.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.request.MostBorrowedBooksFilter;
import net.library.model.response.AnalyticsCountersResponse;
import net.library.model.response.MostBorrowedBookResponse;
import net.library.model.response.Page;
import net.library.service.LibraryAnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

import static net.library.util.HttpUtil.ANALYTICS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ANALYTICS)
public class AnalyticsController {

    private final LibraryAnalyticsService service;

    @Operation(summary = "Get most borrowed books", description = "Returns a paginated, ranked list of the most borrowed books, " +
            "optionally filtered by borrow-action date range, book creation date range and publication year range")
    @GetMapping("/most-borrowed-books")
    public Page<MostBorrowedBookResponse> getMostBorrowedBooks(
            @Valid MostBorrowedBooksFilter filter,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        var pages = service.getMostBorrowedBooks(
                filter,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
        );

        return new Page<>(pages.getSize(), pages.getNumber(), pages.getTotalElements(),
                pages.get().collect(Collectors.toList()));
    }

    @Operation(summary = "Get analytics counters", description = "Returns total counts of books, distinct authors and borrow actions")
    @GetMapping("/counters")
    public ResponseEntity<AnalyticsCountersResponse> getCounters() {
        return ResponseEntity.ok(service.getCounters());
    }
}
