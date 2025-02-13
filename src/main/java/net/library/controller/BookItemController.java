package net.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.dto.BookItemDto;
import net.library.model.dto.BookItemIdDto;
import net.library.model.dto.Page;
import net.library.model.request.BookItemRequest;
import net.library.repository.enums.BookItemStatus;
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

import static net.library.util.HttpUtil.ITEMS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ITEMS)
public class BookItemController {
    private final BookService service;

    @Operation(summary = "Add  a book item(copy) to database", description = "saves a book item(copy) to database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "400", description = "when mandatory param is missing")
    }
    )
    @PostMapping
    public ResponseEntity<BookItemDto> addBookItems(@Valid @RequestBody BookItemRequest bookItemRequest) {

        return ResponseEntity.status(201).body(service.addBookItem(bookItemRequest));
    }

    @Operation(summary = "Update book item status to BORROWED, and populating  a borrower", description = "updates status and sets a" +
            " borrower for the book  and SETTING BORROWED DATE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successfully updated"),
            @ApiResponse(responseCode = "404", description = "not found - when id of book item is not found")
    }
    )
    @PatchMapping("/{bookItemId}/borrowing")
    public ResponseEntity<Void> borrowBookItem(@PathVariable(required = false, value = "bookItemId") final UUID bookItemId,
                                                     @RequestParam("userId") UUID userId,
                                                     @RequestParam("status") BookItemStatus status
    ) {
        service.borrowActionBookItemById(bookItemId, userId, status);
        return ResponseEntity.status(202).build();
    }

    @PatchMapping("/{bookId}/borrowingAny")
    public ResponseEntity<BookItemIdDto> borrowAnyBookItem(@PathVariable(required = false, value = "bookId") final UUID bookId,
                                                                  @RequestParam("userId") UUID userId
    ) {
        return ResponseEntity.status(202).body(service.borrowActionForAnyBookItem(bookId, userId));
    }

    @Operation(summary = "Update book item status to RETURNED", description = "updates status to RETURNED and SETTING RETURNED DATE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successfully updated"),
            @ApiResponse(responseCode = "404", description = "not found - when id of book item is not found")
    }
    )
    @PatchMapping("/{bookItemId}/return")
    public ResponseEntity<Void> returnBookItem(@PathVariable(required = false, value = "bookItemId") final UUID bookItemId,
                                                     @RequestParam("userId") UUID userId
    ) {
        service.returnActionForBookItem(bookItemId, userId);
        return ResponseEntity.status(202).build();
    }

    @Operation(summary = "Get  all available book items(copies), with adjustable sorting( default by createdAt",
            description = "retrieve  all available book items(copies), with adjustable sorting( default by createdAt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    }
    )
    @GetMapping
    public Page<BookItemDto> getAllAvailableBookItems(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "bookItemId") UUID bookItemId,
            @RequestParam(required = false, name = "bookId") UUID bookId,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        var sortBy = params.get("sortBy");
        var sortFields = sortBy != null && !sortBy.isEmpty()
                ? List.of(sortBy.split(","))
                : List.of("createdAt");

        var direction = Sort.Direction.fromOptionalString(params.get("order")).orElse(Sort.Direction.DESC);
        var sort = Sort.by(sortFields.stream().map(field -> new Sort.Order(direction, field)).toList());

        var availableBooks = service.getBookItems(bookItemId, bookId, params.get("status"), params.get("startDate"),
                params.get("endDate"), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));

        return new Page<>(availableBooks.getSize(), availableBooks.getNumber(), availableBooks.getTotalElements(),
                availableBooks.get().collect(Collectors.toList()));
    }
}
