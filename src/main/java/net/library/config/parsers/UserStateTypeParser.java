package net.library.config.parsers;

import net.library.repository.enums.UserState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class UserStateTypeParser implements Converter<String, UserState> {

    @Override
    public UserState convert(@NonNull final String value) {
        return Arrays.stream(UserState.values())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + value + "'"));
    }
}
