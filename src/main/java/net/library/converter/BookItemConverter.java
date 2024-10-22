package net.library.converter;

import net.library.model.entity.BookItem;
import net.library.model.request.BookItemRequest;

public class BookItemConverter {

    public static BookItem of(final BookItemRequest bookItemRequest) {
        var bookItem = new BookItem();
        bookItem.setBookId(bookItemRequest.getBookId());

        return bookItem;
    }
}