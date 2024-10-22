package net.library.config.parsers;

import net.library.repository.enums.RoleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class RoleTypeTypeParser implements Converter<String, RoleType> {

    @Override
    public RoleType convert(@NonNull final String value) {
        return Arrays.stream(RoleType.values())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + value + "'"));
    }
}
