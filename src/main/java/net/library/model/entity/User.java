package net.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id") // Lowercase, unquoted, ensures case insensitivity
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "moderation_state", columnDefinition = "moderation_state_type",insertable = false, updatable = true)
    private ModerationState moderationState ;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "user_state", columnDefinition = "user_state_type",insertable = false, updatable = true)
    private UserState userState;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role_type", columnDefinition = "role_type_type",insertable = false, updatable = true)
    private RoleType roleType;

    @Column(name = "updated_at", insertable = false, updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
