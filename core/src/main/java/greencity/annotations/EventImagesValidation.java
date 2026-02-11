package greencity.annotations;

import greencity.validator.EventImagesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EventImagesValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventImagesValidation {
    String message() default "Invalid event images";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
