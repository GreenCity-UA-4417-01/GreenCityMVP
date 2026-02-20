package greencity.validator;

import greencity.annotations.EventImagesValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

public class EventImagesValidator implements ConstraintValidator<EventImagesValidation, List<MultipartFile>> {
    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    @Override
    public boolean isValid(List<MultipartFile> images, ConstraintValidatorContext context) {
        if (images == null || images.isEmpty()) {
            return true;
        }

        long nonEmptyCount = images.stream()
            .filter(f -> f != null && !f.isEmpty())
            .count();

        if (nonEmptyCount > MAX_IMAGES) {
            return buildViolation(context,
                "Maximum allowed images count is " + MAX_IMAGES + ". Provided: " + nonEmptyCount);
        }

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return buildViolation(context,
                    "Unsupported image content type: " + contentType + ". Allowed formats: JPG, PNG");
            }

            if (file.getSize() > MAX_IMAGE_BYTES) {
                long mb = file.getSize() / 1024 / 1024;
                return buildViolation(context,
                    "Incorrect image size. Maximum allowed size is 10 MB. Image '"
                        + file.getOriginalFilename() + "' is " + mb + " MB");
            }
        }

        return true;
    }

    private boolean buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
