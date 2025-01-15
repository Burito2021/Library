package net.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.dto.BookItemDto;
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

    @PostMapping
    public ResponseEntity<BookItemDto> addBookItems(@Valid @RequestBody BookItemRequest bookItemRequest) {

        return ResponseEntity.status(201).body(service.addBookItem(bookItemRequest));
    }

    @PatchMapping("/{bookItemId}/borrowing")
    public ResponseEntity<Void> updateBookItemBorrow(@PathVariable(required = false, value = "bookItemId") final UUID bookItemId,
                                                     @RequestParam("userId") UUID userId,
                                                     @RequestParam("status") BookItemStatus status
    ) {
        service.updateBookItemBorrow(bookItemId, userId, status);
        return ResponseEntity.status(202).build();
    }

    @PatchMapping("/{bookItemId}/return")
    public ResponseEntity<Void> updateBookItemReturn(@PathVariable(required = false, value = "bookItemId") final UUID bookItemId,
                                                     @RequestParam("userId") UUID userId
    ) {
        service.updateBookItemReturn(bookItemId, userId);
        return ResponseEntity.status(202).build();
    }

    @GetMapping
    public Page<BookItemDto> getAllAvailableBookItems(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "bookItemId") UUID bookItemId,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        var sortBy = params.get("sortBy");
        var sortFields = sortBy != null && !sortBy.isEmpty()
                ? List.of(sortBy.split(","))
                : List.of("createdAt");

        var direction = Sort.Direction.fromOptionalString(params.get("order")).orElse(Sort.Direction.DESC);
        var sort = Sort.by(sortFields.stream().map(field -> new Sort.Order(direction, field)).toList());

        var availableBooks = service.getAllAvailableBooks(bookItemId, params.get("status"), params.get("startDate"),
                params.get("endDate"), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));

        return new Page<>(availableBooks.getSize(), availableBooks.getNumber(), availableBooks.getTotalElements(),
                availableBooks.get().collect(Collectors.toList()));
    }
}
