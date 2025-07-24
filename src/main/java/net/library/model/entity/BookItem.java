package net.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import net.library.repository.enums.BookItemStatus;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "book_items")
public class BookItem {

//    @Version
//    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "book_id")
    private UUID bookId;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", insertable = false)
    private BookItemStatus status;

    @OneToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User userId;

    @Column(name = "borrowed_at", insertable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "returned_at", insertable = false)
    private LocalDateTime returnedAt;

    @Column(name = "updated_at", insertable = false, updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", insertable = false, updatable = true)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", insertable = false)
    private LocalDateTime deletedAt;

    @Column(name = "due_date", columnDefinition = "DATE")
    private LocalDate dueDate;

    @Version
    private Long version;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
