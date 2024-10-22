package net.library.config.parsers;

import net.library.repository.enums.BookItemStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class BookItemStatusTypeParser implements Converter<String, BookItemStatus> {

    @Override
    public BookItemStatus convert(@NonNull final String value) {
        return Arrays.stream(BookItemStatus.values())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + value + "'"));
    }
}
