package greencity.validator;

import greencity.annotations.ValidEventLocation;
import greencity.dto.event.EventDateLocationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class EventLocationValidator
    implements ConstraintValidator<ValidEventLocation, EventDateLocationRequestDto> {
    @Override
    public boolean isValid(EventDateLocationRequestDto value,
        ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        BigDecimal lat = value.getLatitude();
        BigDecimal lon = value.getLongitude();
        String link = value.getOnlineLink();

        boolean hasLat = lat != null;
        boolean hasLon = lon != null;
        boolean hasCoords = hasLat && hasLon;
        boolean hasLink = link != null && !link.isBlank();

        if (hasLat ^ hasLon) {
            valid = false;
            context.buildConstraintViolationWithTemplate(
                "Latitude and longitude must be provided together").addConstraintViolation();
        }

        if (!hasCoords && !hasLink) {
            valid = false;
            context.buildConstraintViolationWithTemplate(
                "Either onlineLink or coordinates must be provided").addConstraintViolation();
        }
        return valid;
    }
}
