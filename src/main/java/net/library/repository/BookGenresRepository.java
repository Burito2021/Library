package net.library.repository;

import net.library.model.entity.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookGenresRepository extends JpaRepository<BookGenre, UUID> {
}
