package net.library.repository;

import net.library.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only analytics queries, written as explicit PostgreSQL so the SQL that runs is the SQL that is read.
 */
public interface LibraryAnalyticsRepository extends Repository<Book, UUID> {

    // Shared by the ranking query and its count query, so the filtering is maintained in one place.
    // Only BORROWED history rows are counted: ADDED, RETURNED and other actions are not borrows.
    // The CAST in each IS NULL check is needed because PostgreSQL cannot infer the type of a null parameter.
    String BORROWED_BOOKS_FILTERS = """
            FROM book_item_history h
            JOIN book_items i ON i.id = h.item_id
            JOIN books b ON b.id = i.book_id
            WHERE h.action_type = 'BORROWED'
              AND (CAST(:actionFrom AS TIMESTAMP) IS NULL OR h.action_at >= :actionFrom)
              AND (CAST(:actionTo AS TIMESTAMP) IS NULL OR h.action_at <= :actionTo)
              AND (CAST(:createdFrom AS TIMESTAMP) IS NULL OR b.created_at >= :createdFrom)
              AND (CAST(:createdTo AS TIMESTAMP) IS NULL OR b.created_at <= :createdTo)
              AND (CAST(:publicationYearFrom AS INTEGER) IS NULL OR b.publication_year >= :publicationYearFrom)
              AND (CAST(:publicationYearTo AS INTEGER) IS NULL OR b.publication_year <= :publicationYearTo)
            """;

    /**
     * Ranks books by borrow count. All filters are optional: a {@code null} filter is ignored.
     * Books with the same count are ordered by id, so the order is stable across page requests.
     */
    @Query(value = "SELECT b.id AS \"bookId\", b.title AS \"title\", b.author AS \"author\", "
            + "b.publication_year AS \"publicationYear\", COUNT(h.id) AS \"borrowCount\" "
            + BORROWED_BOOKS_FILTERS
            + "GROUP BY b.id, b.title, b.author, b.publication_year "
            + "ORDER BY COUNT(h.id) DESC, b.id ASC",
            countQuery = "SELECT COUNT(DISTINCT b.id) " + BORROWED_BOOKS_FILTERS,
            nativeQuery = true)
    Page<MostBorrowedBook> findMostBorrowedBooks(
            @Param("actionFrom") LocalDateTime actionFrom,
            @Param("actionTo") LocalDateTime actionTo,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("publicationYearFrom") Integer publicationYearFrom,
            @Param("publicationYearTo") Integer publicationYearTo,
            Pageable pageable
    );

    /**
     * All counters in one statement, so they are read from the same database snapshot and cannot disagree
     * with each other when books or borrows are written concurrently.
     */
    @Query(value = """
            SELECT (SELECT COUNT(*) FROM books) AS "totalBooks",
                   (SELECT COUNT(DISTINCT author) FROM books) AS "totalAuthors",
                   (SELECT COUNT(*) FROM book_item_history WHERE action_type = 'BORROWED') AS "totalBorrows"
            """, nativeQuery = true)
    Counters getCounters();

    interface MostBorrowedBook {
        UUID getBookId();
        String getTitle();
        String getAuthor();
        Integer getPublicationYear();
        long getBorrowCount();
    }

    interface Counters {
        long getTotalBooks();
        long getTotalAuthors();
        long getTotalBorrows();
    }
}
