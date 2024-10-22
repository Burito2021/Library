package net.library.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class BookItemDto {

    private UUID bookItemId;

    private UUID bookId;

    private UUID userId;

    private LocalDateTime borrowedAt;

    private LocalDateTime returnedAt;
}
