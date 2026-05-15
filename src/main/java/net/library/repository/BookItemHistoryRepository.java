package net.library.repository;

import net.library.model.entity.BookItemHistory;
import net.library.repository.enums.BookAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BookItemHistoryRepository extends JpaRepository<BookItemHistory, UUID> {

    // Shared by the main query and the count query, so the filtering logic is maintained in one place.
    // Both are compile-time constants, so they can be used inside the annotation.
    String BORROWED_BOOKS_FILTERS = """
                FROM BookItemHistory h
                JOIN BookItem i ON i.id = h.itemId
                JOIN Book b ON b.id = i.bookId
                WHERE h.actionType = :actionType
                  AND (CAST(:actionFrom AS LocalDateTime) IS NULL OR h.actionAt >= :actionFrom)
                  AND (CAST(:actionTo AS LocalDateTime) IS NULL OR h.actionAt <= :actionTo)
                  AND (CAST(:createdFrom AS LocalDateTime) IS NULL OR b.createdAt >= :createdFrom)
                  AND (CAST(:createdTo AS LocalDateTime) IS NULL OR b.createdAt <= :createdTo)
                  AND (CAST(:publicationYearFrom AS Integer) IS NULL OR b.publicationYear >= :publicationYearFrom)
                  AND (CAST(:publicationYearTo AS Integer) IS NULL OR b.publicationYear <= :publicationYearTo)
                """;

    /**
     * Ranks books by the number of history records of the given action type. All filters are optional:
     * a {@code null} filter is ignored.
     * <p>
     * This is JPQL, not native SQL: it works with entities and fields instead of table and column names, and the
     * action type is compared as a {@link BookAction} enum instead of a string literal.
     * <p>
     * The CAST is still needed in the {@code IS NULL} check. Hibernate does not take the parameter type from the
     * other place where the same parameter is used ({@code h.actionAt >= :actionFrom}), so a {@code null} is sent
     * to PostgreSQL untyped and it fails with "could not determine data type of parameter". In the comparison
     * the type is known from the field, so no CAST is needed there.
     */
    @Query(value = "SELECT b.id AS bookId, b.title AS title, b.author AS author, "
            + "b.publicationYear AS publicationYear, COUNT(h.id) AS borrowCount "
            + BORROWED_BOOKS_FILTERS
            + "GROUP BY b.id, b.title, b.author, b.publicationYear ORDER BY COUNT(h.id) DESC",
            countQuery = "SELECT COUNT(DISTINCT b.id) " + BORROWED_BOOKS_FILTERS)
    Page<MostBorrowedBookProjection> findMostBooksByAction(
            @Param("actionType") BookAction actionType,
            @Param("actionFrom") LocalDateTime actionFrom,
            @Param("actionTo") LocalDateTime actionTo,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("publicationYearFrom") Integer publicationYearFrom,
            @Param("publicationYearTo") Integer publicationYearTo,
            Pageable pageable
    );

    default Page<MostBorrowedBookProjection> findMostBorrowedBooks(
            LocalDateTime actionFrom, LocalDateTime actionTo,
            LocalDateTime createdFrom, LocalDateTime createdTo,
            Integer publicationYearFrom, Integer publicationYearTo,
            Pageable pageable
    ) {
        return findMostBooksByAction(BookAction.BORROWED, actionFrom, actionTo, createdFrom, createdTo,
                publicationYearFrom, publicationYearTo, pageable);
    }

    long countByActionType(BookAction actionType);

    default long countTotalBorrows() {
        return countByActionType(BookAction.BORROWED);
    }

    interface MostBorrowedBookProjection {
        UUID getBookId();
        String getTitle();
        String getAuthor();
        Integer getPublicationYear();
        long getBorrowCount();
    }
}
