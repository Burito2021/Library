package net.library.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.model.entity.User;
import net.library.model.request.UserRequest;
import net.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class Tools {

    @Autowired
    private UserService service;

    public static String objectToStringConverter(User user) {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Impossible to convert into string");
        }
    }

    public static UserRequest generateUser(
            String username,
            String name,
            String surname,
            String email,
            String phoneNumber,
            String address) {
        return new UserRequest(username, name, surname, email, phoneNumber, address);
    }

    public static String randomMsisdn(String cc, int count) {
        var ndc = randomNumeric(2);
        var sn = randomNumeric(count);
        return cc + ndc + sn;
    }

    public static String randomString(int length) {
        return randomAlphabetic(length);
    }

    public void insertUsers(UserRequest userRequest) {

        service.addUser(userRequest);
    }

    public void generateUsersInDb(int countOfUsers) {
        for (int x = 0; x < countOfUsers; x++) {
            var name = randomString(8);
            var surname = randomString(10);
            var username = name + surname;
            var email = name + surname + "@" + "gmail.com";
            var phoneNumber = randomMsisdn("380", 7);
            var address = randomString(15);
            ;
            insertUsers(generateUser(username, name, surname, email, phoneNumber, address));
        }
    }
}