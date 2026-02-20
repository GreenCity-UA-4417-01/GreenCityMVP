package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventImagesValidatorTest {

    private static final long TEN_MB = 10L * 1024 * 1024;

    private EventImagesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventImagesValidator();
    }

    @Test
    void isValid_shouldReturnTrue_whenImagesIsNull() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenImagesIsEmptyList() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(List.of(), context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenAllFilesAreNullOrEmpty() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        MultipartFile empty1 = mock(MultipartFile.class);
        when(empty1.isEmpty()).thenReturn(true);

        MultipartFile empty2 = mock(MultipartFile.class);
        when(empty2.isEmpty()).thenReturn(true);

        boolean result = validator.isValid(Arrays.asList(null, empty1, empty2), context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnTrue_whenUpTo5NonEmptyFilesAndAllValid() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        List<MultipartFile> images = List.of(
            nonEmptyFile("a.jpg", "image/jpeg", TEN_MB),
            nonEmptyFile("b.png", "image/png", TEN_MB - 1),
            nonEmptyFile("c.png", "image/png", 1),
            nonEmptyFile("d.jpg", "image/jpeg", 123_456),
            nonEmptyFile("e.png", "image/png", 5_000));

        boolean result = validator.isValid(images, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnFalse_whenNonEmptyCountExceedsMaxImages() {
        ViolationHarness harness = violationHarness();

        List<MultipartFile> images = new ArrayList<>();
        images.add(nonEmptyFile("1.jpg", "image/jpeg", 1));
        images.add(nonEmptyFile("2.jpg", "image/jpeg", 1));
        images.add(nonEmptyFile("3.png", "image/png", 1));
        images.add(nonEmptyFile("4.png", "image/png", 1));
        images.add(nonEmptyFile("5.jpg", "image/jpeg", 1));
        images.add(nonEmptyFile("6.png", "image/png", 1));

        boolean result = validator.isValid(images, harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Maximum allowed images count is 5. Provided: 6");
        verify(harness.builder).addConstraintViolation();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"image/gif", "text/plain", "application/octet-stream"})
    void isValid_shouldReturnFalse_whenContentTypeIsInvalid(String contentType) {
        ViolationHarness harness = violationHarness();

        MultipartFile file = nonEmptyFile("a.png", contentType, 1);

        boolean result = validator.isValid(List.of(file), harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Unsupported image content type: " + contentType + ". Allowed formats: JPG, PNG");
        verify(harness.builder).addConstraintViolation();
    }

    @Test
    @DisplayName("isValid should return true when file size is exactly 10 MB")
    void isValid_shouldReturnTrue_whenFileSizeIsExactly10Mb() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        MultipartFile file = nonEmptyFile("a.png", "image/png", TEN_MB);

        boolean result = validator.isValid(List.of(file), context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_shouldReturnFalse_whenFileSizeExceeds10Mb() {
        ViolationHarness harness = violationHarness();

        MultipartFile file = nonEmptyFile("a.png", "image/png", TEN_MB + 1);

        boolean result = validator.isValid(List.of(file), harness.context);

        assertFalse(result);
        verify(harness.context).disableDefaultConstraintViolation();
        verify(harness.context).buildConstraintViolationWithTemplate(
            "Incorrect image size. Maximum allowed size is 10 MB. Image 'a.png' is 10 MB");
        verify(harness.builder).addConstraintViolation();
    }

    private MultipartFile nonEmptyFile(String filename, String contentType, long sizeBytes) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getSize()).thenReturn(sizeBytes);
        return file;
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
