package net.library.service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidationService.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValidator {
    String message() default "INVALID PASSWORD FORMAT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
