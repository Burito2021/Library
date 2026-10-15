package net.library.service;

import net.library.model.request.MostBorrowedBooksFilter;
import net.library.repository.BookItemHistoryRepository;
import net.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryAnalyticsServiceTest {

    @InjectMocks
    private LibraryAnalyticsService analyticsService;
    @Mock
    private BookItemHistoryRepository bookItemHistoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookItemHistoryRepository.MostBorrowedBookProjection projection;

    private final UUID bookId = UUID.randomUUID();

    @Test
    void getMostBorrowedBooksReturnsMappedPage() {
        final var pageable = PageRequest.of(0, 10);
        when(projection.getBookId()).thenReturn(bookId);
        when(projection.getTitle()).thenReturn("The Great Gatsby");
        when(projection.getAuthor()).thenReturn("F. Scott Fitzgerald");
        when(projection.getPublicationYear()).thenReturn(1925);
        when(projection.getBorrowCount()).thenReturn(5L);

        when(bookItemHistoryRepository.findMostBorrowedBooks(
                any(), any(), any(), any(), any(), any(), eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        var result = analyticsService.getMostBorrowedBooks(
                new MostBorrowedBooksFilter(
                        LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 0, 0),
                        LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2020, 12, 31, 0, 0),
                        1900, 2000),
                pageable
        );

        assertEquals(1, result.getTotalElements());
        var response = result.getContent().getFirst();
        assertEquals(bookId, response.getBookId());
        assertEquals("The Great Gatsby", response.getTitle());
        assertEquals("F. Scott Fitzgerald", response.getAuthor());
        assertEquals(1925, response.getPublicationYear());
        assertEquals(5L, response.getBorrowCount());
    }

    @Test
    void getMostBorrowedBooksPassesEmptyFilterAsNulls() {
        final var pageable = PageRequest.of(0, 10);
        when(bookItemHistoryRepository.findMostBorrowedBooks(
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = analyticsService.getMostBorrowedBooks(
                new MostBorrowedBooksFilter(null, null, null, null, null, null), pageable
        );

        assertEquals(0, result.getTotalElements());
        verify(bookItemHistoryRepository).findMostBorrowedBooks(
                null, null, null, null, null, null, pageable
        );
    }

    @Test
    void getMostBorrowedBooksReturnsEmptyPageWhenNoResults() {
        final var pageable = PageRequest.of(0, 10);
        when(bookItemHistoryRepository.findMostBorrowedBooks(
                any(), any(), any(), any(), any(), any(), eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = analyticsService.getMostBorrowedBooks(
                new MostBorrowedBooksFilter(null, null, null, null, null, null), pageable
        );

        assertEquals(0, result.getTotalElements());
        assertEquals(List.of(), result.getContent());
    }

    @Test
    void getCountersReturnsAggregatedCounters() {
        when(bookRepository.count()).thenReturn(42L);
        when(bookRepository.countDistinctAuthors()).thenReturn(7L);
        when(bookItemHistoryRepository.countTotalBorrows()).thenReturn(123L);

        var result = analyticsService.getCounters();

        assertEquals(42L, result.getTotalBooks());
        assertEquals(7L, result.getTotalAuthors());
        assertEquals(123L, result.getTotalBorrows());
    }

    @Test
    void getCountersReturnsZerosWhenNoData() {
        when(bookRepository.count()).thenReturn(0L);
        when(bookRepository.countDistinctAuthors()).thenReturn(0L);
        when(bookItemHistoryRepository.countTotalBorrows()).thenReturn(0L);

        var result = analyticsService.getCounters();

        assertEquals(0L, result.getTotalBooks());
        assertEquals(0L, result.getTotalAuthors());
        assertEquals(0L, result.getTotalBorrows());
    }
}
