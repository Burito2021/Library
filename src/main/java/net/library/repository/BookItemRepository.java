package net.library.repository;

import net.library.model.entity.BookItem;
import net.library.repository.enums.BookItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookItemRepository extends JpaRepository<BookItem, UUID>, JpaSpecificationExecutor<BookItem> {

    //add description or change names think action
    @Modifying
    @Query("UPDATE BookItem u SET u.userId=:userId,u.status=:status, u.borrowedAt=:currentTime,u.returnedAt=NULL,u.updatedAt=CURRENT_TIMESTAMP, u.dueDate=:dueDate WHERE u.id = :bookItemId")
    int borrowAction(@Param("bookItemId") UUID bookItemId, @Param("userId") UUID userId, @Param("status") BookItemStatus status, LocalDateTime currentTime, LocalDate dueDate);

    @Modifying
    @Query("UPDATE BookItem u SET u.status='AVAILABLE', u.returnedAt=:currentTime,u.updatedAt=CURRENT_TIMESTAMP, u.dueDate=NULL WHERE u.userId=:userId AND u.id=:bookItemId")
    int returnAction(@Param("bookItemId") UUID bookItemId, @Param("userId") UUID userId, LocalDateTime currentTime);

    @Query("SELECT u FROM BookItem u WHERE u.status = 'AVAILABLE' AND u.bookId = :bookId")
    List<BookItem> findAvailableBooksByBookId(@Param("bookId") UUID bookId);

    @Modifying
    @Query(value = """
                WITH available AS (
                    SELECT id FROM book_items 
                    WHERE status = 'AVAILABLE' AND book_id = :bookId
                    LIMIT 1
                )
                UPDATE book_items
                SET user_id = :userId, 
                    status = 'IN_PROGRESS', 
                    borrowed_at = :currentTime, 
                    returned_at = NULL, 
                    updated_at = CURRENT_TIMESTAMP, 
                    due_date = :dueDate
                WHERE id IN (SELECT id FROM available)
                RETURNING id;
            """, nativeQuery = true)
    List<UUID> selectAvailableBookItemIdAndUpdate(
            @Param("bookId") UUID bookId,
            @Param("userId") UUID userId,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("dueDate") LocalDate dueDate
    );
}
