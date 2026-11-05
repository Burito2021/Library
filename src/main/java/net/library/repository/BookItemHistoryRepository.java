package net.library.repository;

import net.library.model.entity.BookItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookItemHistoryRepository extends JpaRepository<BookItemHistory, UUID> {
}
