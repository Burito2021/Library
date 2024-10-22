package net.library.config.parsers;

import net.library.repository.ModerationState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class ModerationTypeParser implements Converter<String, ModerationState> {

    @Override
    public ModerationState convert(@NonNull final String value) {
        return Arrays.stream(ModerationState.values())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + value + "'"));
    }
}
