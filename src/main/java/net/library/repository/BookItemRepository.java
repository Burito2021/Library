package net.library.repository;

import net.library.model.entity.BookItem;
import net.library.repository.enums.BookItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BookItemRepository extends JpaRepository<BookItem, UUID>, JpaSpecificationExecutor<BookItem> {
    @Modifying
    @Query("UPDATE BookItem u SET u.userId=:userId,u.status=:status, u.borrowedAt=:currentTime,u.returnedAt=NULL,u.updatedAt=CURRENT_TIMESTAMP WHERE u.id = :bookItemId")
    int updateBookItemBorrow(@Param("bookItemId") UUID bookItemId, @Param("userId") UUID userId, @Param("status") BookItemStatus status, LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE BookItem u SET u.status='AVAILABLE', u.returnedAt=:currentTime,u.updatedAt=CURRENT_TIMESTAMP WHERE u.userId=:userId AND u.id=:bookItemId")
    int updateBookItemReturn(@Param("bookItemId") UUID bookItemId, @Param("userId") UUID userId, LocalDateTime currentTime);
}
