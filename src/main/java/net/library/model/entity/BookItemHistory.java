package net.library.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import net.library.repository.enums.BookAction;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "book_item_history")
public class BookItemHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "action_type")
    private BookAction actionType;
}
