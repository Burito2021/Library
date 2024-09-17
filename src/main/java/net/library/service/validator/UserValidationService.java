package net.library.service.validator;

import net.library.exception.UserAlreadyExistsException;
import net.library.exception.WrongMsisdnException;
import net.library.model.dto.UserDto;

import java.util.List;
import java.util.regex.Pattern;

public class UserValidationService {
    protected static final Pattern PATTERN1 = Pattern.compile("380\\d{9}");
    protected static final Pattern PATTERN2 = Pattern.compile("380\\d{8}");
    protected static final Pattern PATTERN3 = Pattern.compile("380\\d{7}");
    protected static final Pattern NUMBERS_PATTERN = Pattern.compile("[0-9]+");


    private static boolean ifDigit(String msisdn) {
        return NUMBERS_PATTERN.matcher(msisdn).matches();
    }

    private static boolean isLengthValid(String value, int min, int max) {
        var length = value.length();
        return length >= min && length <= max;
    }

    public static void lengthValidator(String msisdn, int min, int max) {
        if (ifDigit(msisdn) && isLengthValid(msisdn, min, max)) {
            return;
        }
        throw new WrongMsisdnException("WRONG MSISDN LENGTH AND NOT DIGIT");
    }

    public static void msisdnCheckFormat380(String msisdn) {
        if (PATTERN1.matcher(msisdn).matches() || PATTERN2.matcher(msisdn).matches() || PATTERN3.matcher(msisdn).matches()) {
            return;
        }
        throw new WrongMsisdnException("WRONG MSISDN FORMAT");
    }

    public static void usernameValidator(List<UserDto> listOfUsers, String userName) {
        for (UserDto userDto : listOfUsers
        ) {
            if (userDto.getUsername().equals(userName)) {
                throw new UserAlreadyExistsException("Username already exists in DB");
            }
        }
    }
}
