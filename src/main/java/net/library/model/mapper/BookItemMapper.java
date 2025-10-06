package net.library.model.mapper;

import net.library.model.dto.BookItemDto;
import net.library.model.dto.UserDto;
import net.library.model.entity.BookItem;

import java.util.List;
import java.util.stream.Collectors;


public class BookItemMapper {

    public static BookItemDto toDto(BookItem bookItem) {

        return new BookItemDto(bookItem.getId(),
                null,
                bookItem.getStatus(), bookItem.getBorrowedAt(), bookItem.getReturnedAt());
    }

    public static List<BookItemDto> toDto(List<BookItem> bookItems) {
        return bookItems.stream().map(BookItemMapper::toDto).collect(Collectors.toList());
    }
}
