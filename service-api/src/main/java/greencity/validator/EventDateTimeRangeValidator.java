package greencity.validator;

import greencity.annotations.ValidEventDateTimeRange;
import greencity.dto.event.EventDateLocationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;

public class EventDateTimeRangeValidator
    implements ConstraintValidator<ValidEventDateTimeRange, EventDateLocationRequestDto> {
    @Override
    public boolean isValid(EventDateLocationRequestDto value,
        ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        OffsetDateTime start = value.getStartDateTime();
        OffsetDateTime end = value.getEndDateTime();

        if (start != null && start.isBefore(OffsetDateTime.now())) {
            valid = false;
            context.buildConstraintViolationWithTemplate(
                "Start date/time must not be in the past").addPropertyNode("startDateTime")
                .addConstraintViolation();
        }

        if (start != null && end != null && !end.isAfter(start)) {
            valid = false;
            context.buildConstraintViolationWithTemplate(
                "End date/time must be after start date/time").addPropertyNode("endDateTime")
                .addConstraintViolation();
        }
        return valid;
    }
}
