package net.library.service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.library.exception.FilterLengthException;
import net.library.exception.WrongState;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static net.library.util.Utils.deleteSpacesHyphens;

public class UserValidationService implements ConstraintValidator<PhoneNumberValidator, String> {
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

    public static void isLength(String value, int min) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            return;
        }
        var length = value.length();
        if (length < min) {
            throw new FilterLengthException("filer length is less that 3 letters");
        }
    }

    public static boolean lengthValidator(String msisdn, int min, int max) {
        return ifDigit(msisdn) && isLengthValid(msisdn, min, max);
    }

    public static boolean msisdnCheckFormat380(String msisdn) {
        return PATTERN1.matcher(msisdn).matches()
                || PATTERN2.matcher(msisdn).matches()
                || PATTERN3.matcher(msisdn).matches();
    }

    public static void isValidModerationState(String state) throws WrongState {

        if (!Arrays.asList("ON_REVIEW", "APPROVED", "DECLINED").contains(state.toUpperCase())) {
            throw new WrongState(format("Passed state is wrong: %s", state));
        }
    }

    public static void isValidUserState(String state) {
        if (!Arrays.asList("ACTIVE", "BANNED", "SUSPENDED").contains(state.toUpperCase())) {
            throw new WrongState(format("Passed state is wrong: %s", state));
        }
    }

    public static void isValidRoleType(String role) {
        if (!Arrays.asList("USER", "ADMIN").contains(role.toUpperCase())) {
            throw new WrongState(format("Passed role is wrong: %s", role));
        }
    }

    @Override
    public void initialize(PhoneNumberValidator constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        final var msisdn = deleteSpacesHyphens(value);
        return lengthValidator(msisdn, 10, 12) && msisdnCheckFormat380(msisdn);
    }
}