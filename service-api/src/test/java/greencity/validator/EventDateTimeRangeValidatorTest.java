package greencity.validator;

import greencity.dto.event.EventDateLocationRequestDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventDateTimeRangeValidatorTest {

    private EventDateTimeRangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventDateTimeRangeValidator();
    }

    @Test
    void isValid_shouldReturnTrue_whenValueIsNull() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenStartAndEndAreValid() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        OffsetDateTime start = OffsetDateTime.now().plusMinutes(10);
        OffsetDateTime end = start.plusMinutes(30);

        when(dto.getStartDateTime()).thenReturn(start);
        when(dto.getEndDateTime()).thenReturn(end);

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_shouldReturnFalse_whenStartIsInThePast() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        when(dto.getStartDateTime()).thenReturn(OffsetDateTime.now().minusMinutes(10));
        when(dto.getEndDateTime()).thenReturn(null);

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();

        verify(harness.context).buildConstraintViolationWithTemplate("Start date/time must not be in the past");
        verify(harness.builder).addPropertyNode("startDateTime");
        verify(harness.nodeBuilder).addConstraintViolation();
    }

    @Test
    void isValid_shouldReturnFalse_whenEndIsNotAfterStart() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        OffsetDateTime start = OffsetDateTime.now().plusMinutes(10);

        when(dto.getStartDateTime()).thenReturn(start);
        when(dto.getEndDateTime()).thenReturn(start);

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();

        verify(harness.context).buildConstraintViolationWithTemplate("End date/time must be after start date/time");
        verify(harness.builder).addPropertyNode("endDateTime");
        verify(harness.nodeBuilder).addConstraintViolation();
    }

    @Test
    void isValid_shouldReturnFalse_whenStartIsPastAndEndIsNotAfterStart() {
        ViolationHarness harness = violationHarness();

        EventDateLocationRequestDto dto = mock(EventDateLocationRequestDto.class);
        OffsetDateTime start = OffsetDateTime.now().minusMinutes(10);

        when(dto.getStartDateTime()).thenReturn(start);
        when(dto.getEndDateTime()).thenReturn(start); // end == start -> invalid

        boolean result = validator.isValid(dto, harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();

        verify(harness.context).buildConstraintViolationWithTemplate("Start date/time must not be in the past");
        verify(harness.context).buildConstraintViolationWithTemplate("End date/time must be after start date/time");

        verify(harness.builder).addPropertyNode("startDateTime");
        verify(harness.builder).addPropertyNode("endDateTime");
        verify(harness.nodeBuilder, times(2)).addConstraintViolation();
    }

    private ViolationHarness violationHarness() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ConstraintValidatorContext.ConstraintViolationBuilder builder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        return new ViolationHarness(context, builder, nodeBuilder);
    }

    private record ViolationHarness(
        ConstraintValidatorContext context,
        ConstraintValidatorContext.ConstraintViolationBuilder builder,
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder) {
    }
}
