package net.library.service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.library.exception.UserAlreadyExistsException;
import net.library.model.dto.UserDto;

import java.util.List;
import java.util.regex.Pattern;

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

    public static boolean lengthValidator(String msisdn, int min, int max) {
        return ifDigit(msisdn) && isLengthValid(msisdn, min, max);
    }

    public static boolean msisdnCheckFormat380(String msisdn) {
        return PATTERN1.matcher(msisdn).matches()
                || PATTERN2.matcher(msisdn).matches()
                || PATTERN3.matcher(msisdn).matches();
    }

    @Override
    public void initialize(PhoneNumberValidator constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final var msisdn = deleteSpacesHyphens(value);
        return lengthValidator(msisdn, 10, 12) && msisdnCheckFormat380(msisdn);
    }
}
