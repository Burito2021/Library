package net.library.service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidationService implements ConstraintValidator<PasswordValidator, String> {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$"
    );

    private static boolean isLengthValid(String value, int min, int max) {
        var length = value.length();
        return length >= min && length <= max;
    }

    private static boolean ifValidPattern(String msisdn) {
        return PASSWORD_PATTERN.matcher(msisdn).matches();
    }

    public static boolean patternLengthValidator(String password, int min, int max) {
        if (password == null) {
            return false;
        }
        return ifValidPattern(password) && isLengthValid(password, min, max);
    }

    @Override
    public void initialize(PasswordValidator constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return patternLengthValidator(value, 8, 15);
    }
}
