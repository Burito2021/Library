package net.library.repository;

import jakarta.persistence.criteria.Predicate;
import net.library.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> filterByParam(String username, ModerationState moderationState, UserState userState, RoleType roleType, LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (username != null && !username.isEmpty()) {

                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("username"), "%" + username + "%"));
            }

            if (moderationState != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("text", String.class, root.get("moderationState")),
                                moderationState.name()
                        ));
            }

            if (userState != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("text", String.class, root.get("userState")),
                                userState.name()
                        ));
            }

            if (roleType != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function("text", String.class, root.get("roleType")),
                                roleType.name()
                        ));
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