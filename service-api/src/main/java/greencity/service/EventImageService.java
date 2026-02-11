package greencity.service;

public interface EventImageService {
    byte[] getImageBytes(Long id);

    String getContentType(Long id);
}
