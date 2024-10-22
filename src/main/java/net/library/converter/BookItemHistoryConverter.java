package net.library.converter;

import net.library.model.entity.BookItemHistory;
import net.library.repository.enums.BookAction;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookItemHistoryConverter {

    public static BookItemHistory of(UUID itemId, UUID userId, LocalDateTime actionAt, BookAction bookAction) {
        var bookItemHistory = new BookItemHistory();
        bookItemHistory.setItemId(itemId);
        bookItemHistory.setUserId(userId);
        bookItemHistory.setActionAt(actionAt);
        bookItemHistory.setActionType(bookAction);
        return bookItemHistory;
    }
}