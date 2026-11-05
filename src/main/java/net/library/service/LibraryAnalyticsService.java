package net.library.service;

import lombok.RequiredArgsConstructor;
import net.library.model.request.MostBorrowedBooksFilter;
import net.library.model.response.AnalyticsCountersResponse;
import net.library.model.response.MostBorrowedBookResponse;
import net.library.repository.LibraryAnalyticsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryAnalyticsService {

    private final LibraryAnalyticsRepository analyticsRepository;

    public Page<MostBorrowedBookResponse> getMostBorrowedBooks(MostBorrowedBooksFilter filter, Pageable pageable) {
        var projections = analyticsRepository.findMostBorrowedBooks(
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
        var counters = analyticsRepository.getCounters();

        return new AnalyticsCountersResponse(
                counters.getTotalBooks(), counters.getTotalAuthors(), counters.getTotalBorrows());
    }
}
