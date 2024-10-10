package net.library.service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserValidationService.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumberValidator {
    String message() default "WRONG MSISDN LENGTH OR FORMAT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
