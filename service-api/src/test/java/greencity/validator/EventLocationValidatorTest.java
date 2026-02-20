package greencity.validator;

import greencity.dto.event.EventDateLocationRequestDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventLocationValidatorTest {

    private EventLocationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventLocationValidator();
    }

    @Test
    void isValid_shouldReturnTrue_whenValueIsNull() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenCoordinatesProvided() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getLatitude()).thenReturn(BigDecimal.valueOf(50.45));
        when(dto.getLongitude()).thenReturn(BigDecimal.valueOf(30.52));
        when(dto.getOnlineLink()).thenReturn(null);

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_shouldReturnTrue_whenOnlineLinkProvided() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getLatitude()).thenReturn(null);
        when(dto.getLongitude()).thenReturn(null);
        when(dto.getOnlineLink()).thenReturn("https://meet.com");

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_shouldReturnFalse_whenOnlyLatitudeProvided() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getLatitude()).thenReturn(BigDecimal.ONE);
        when(dto.getLongitude()).thenReturn(null);
        when(dto.getOnlineLink()).thenReturn("https://meet.com");

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);

        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Latitude and longitude must be provided together");
        verify(harness.builder).addConstraintViolation();
    }

    @Test
    void isValid_shouldReturnFalse_whenOnlyLongitudeProvided() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getLatitude()).thenReturn(null);
        when(dto.getLongitude()).thenReturn(BigDecimal.ONE);
        when(dto.getOnlineLink()).thenReturn("https://meet.com");

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);

        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Latitude and longitude must be provided together");
        verify(harness.builder).addConstraintViolation();
    }

    @Test
    void isValid_shouldReturnFalse_whenNoCoordinatesAndNoLink() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getLatitude()).thenReturn(null);
        when(dto.getLongitude()).thenReturn(null);
        when(dto.getOnlineLink()).thenReturn("   ");

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);

        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Either onlineLink or coordinates must be provided");
        verify(harness.builder).addConstraintViolation();
    }

    private ViolationHarness violationHarness() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        return new ViolationHarness(context, builder);
    }

    private record ViolationHarness(
        ConstraintValidatorContext context,
        ConstraintValidatorContext.ConstraintViolationBuilder builder) {
    }
}
