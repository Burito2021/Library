package net.library.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.library.model.entity.User;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
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
                predicate = filterByState(criteriaBuilder, predicate, root, roleType, "moderationState");
            }

            if (userState != null) {
                predicate = filterByState(criteriaBuilder, predicate, root, roleType, "userState");
            }

            if (roleType != null) {
                predicate = filterByState(criteriaBuilder, predicate, root, roleType, "roleType");
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

    private static Predicate filterByState(CriteriaBuilder criteriaBuilder, Predicate predicate, Root<User> root, RoleType roleType, String state) {
        return criteriaBuilder.and(predicate,
                criteriaBuilder.equal(
                        criteriaBuilder.function("text", String.class, root.get(state)),
                        roleType.name()
                ));
    }
}