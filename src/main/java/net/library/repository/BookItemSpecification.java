package net.library.repository;

import jakarta.persistence.criteria.Predicate;
import net.library.model.entity.BookItem;
import net.library.repository.enums.BookItemStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookItemSpecification {

    public static Specification<BookItem> filterBookItemByStatus(UUID bookItemId, BookItemStatus bookItemStatus, LocalDateTime startDate,
                                                                 LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (bookItemStatus != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("text", String.class, root.get("status")),
                                bookItemStatus.name()
                        ));
            }

            if (bookItemId != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("id"), bookItemId));
            }

            if (startDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return predicate;
        };
    }
}