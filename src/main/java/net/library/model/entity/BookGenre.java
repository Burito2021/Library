package net.library.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "book_genres")
public class BookGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "book_id")
    private UUID book_id;

    @OneToOne
    @JoinColumn(name = "genre_id",referencedColumnName = "id")
    private Genre genre;

}
