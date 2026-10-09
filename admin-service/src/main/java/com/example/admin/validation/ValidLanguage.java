package com.example.admin.validation;

import com.example.admin.dto.SupportedLanguage;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidLanguage.LanguageValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLanguage {
    String message() default "preferredLanguage must be one of the supported values: EN, TE, HI";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class LanguageValidator implements ConstraintValidator<ValidLanguage, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().isEmpty()) {
                return true; // Null/empty is accepted for partial updates
            }
            return SupportedLanguage.isValid(value);
        }
    }
}
