package net.library.service.validator.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.library.util.Utils;

import java.util.regex.Pattern;

public class PasswordValidatorService implements ConstraintValidator<PasswordValidator,String> {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$"
    );

    private static boolean ifValidPattern(String msisdn) {
        return PASSWORD_PATTERN.matcher(msisdn).matches();
    }

    public static boolean patternLengthValidator(String password, int min, int max) {
        if (password == null) {
            return false;
        }
        return ifValidPattern(password) && Utils.isLengthValid(password, min, max);
    }

    @Override
    public void initialize(PasswordValidator constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return patternLengthValidator(value,8,15);
    }
}
