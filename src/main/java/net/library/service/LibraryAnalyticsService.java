package net.library.service;

import lombok.RequiredArgsConstructor;
import net.library.model.request.MostBorrowedBooksFilter;
import net.library.model.response.AnalyticsCountersResponse;
import net.library.model.response.MostBorrowedBookResponse;
import net.library.repository.BookItemHistoryRepository;
import net.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryAnalyticsService {

    private final BookItemHistoryRepository bookItemHistoryRepository;
    private final BookRepository bookRepository;

    public Page<MostBorrowedBookResponse> getMostBorrowedBooks(MostBorrowedBooksFilter filter, Pageable pageable) {
        var projections = bookItemHistoryRepository.findMostBorrowedBooks(
                filter.actionFrom(), filter.actionTo(),
                filter.createdFrom(), filter.createdTo(),
                filter.publicationYearFrom(), filter.publicationYearTo(),
                pageable
        );

        return projections.map(p -> new MostBorrowedBookResponse(
                p.getBookId(), p.getTitle(), p.getAuthor(), p.getPublicationYear(), p.getBorrowCount()
        ));
    }

    public AnalyticsCountersResponse getCounters() {
        var totalBooks = bookRepository.count();
        var totalAuthors = bookRepository.countDistinctAuthors();
        var totalBorrows = bookItemHistoryRepository.countTotalBorrows();

        return new AnalyticsCountersResponse(totalBooks, totalAuthors, totalBorrows);
    }
}
