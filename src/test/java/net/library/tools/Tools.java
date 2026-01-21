package net.library.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.model.entity.BookItem;
import net.library.model.entity.User;
import net.library.model.request.UserRequest;
import net.library.repository.BookItemRepository;
import net.library.repository.enums.BookItemStatus;
import net.library.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static net.library.util.HttpUtil.PASSWORD_ADMIN;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class Tools {

    public static String objectToStringConverter(Object object) {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Impossible to convert into string");
        }
    }

    public static String randomString(int length) {

        return randomAlphabetic(length);
    }

    public static void populateWithBookItems(
            BookItemRepository bookItemRepository,
            UUID bookId,
            User user,
            BookItemStatus bookItemStatus,
            LocalDateTime localDateTime,
            int timesToRepeat) {
        for (int x = 0; x < timesToRepeat; x++) {
            final var bookItem = bookItemRepository.save(new BookItem()
                    .setBookId(bookId)
                    .setUserId(user)
                    .setStatus(BookItemStatus.AVAILABLE));
            bookItem.setStatus(bookItemStatus);
            bookItem.setBorrowedAt(localDateTime);
            bookItemRepository.save(bookItem);
        }
    }

    public static List<Integer> threadRunner(int numberOfThreads, List<Callable<Integer>> tasks) {
        var executor = newFixedThreadPool(numberOfThreads);
         List<Integer> statusCodes = new ArrayList<>();
        try {
            executor.invokeAll(tasks).forEach(
                    future -> {
                        try {
                           var result =  future.get();
                            statusCodes.add(result);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            return statusCodes;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }


}