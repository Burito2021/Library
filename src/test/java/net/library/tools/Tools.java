package net.library.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.model.entity.User;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class Tools {

    public static String objectToStringConverter(User user) {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Impossible to convert into string");
        }
    }

    public static String randomString(int length) {

        return randomAlphabetic(length);
    }
}