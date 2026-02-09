package greencity.annotations;

import greencity.validator.EventLocationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventLocationValidator.class)
public @interface ValidEventLocation {
    String message() default "Invalid event location data";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
