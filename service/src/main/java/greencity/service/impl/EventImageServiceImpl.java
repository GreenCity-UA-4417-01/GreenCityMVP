package greencity.service.impl;

import greencity.exception.exceptions.NotFoundException;
import greencity.repository.event.EventImageDataRepository;
import greencity.service.EventImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventImageServiceImpl implements EventImageService {
    private final EventImageDataRepository repository;

    @Override
    public byte[] getImageBytes(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Event image not found: " + id))
            .getData();
    }

    @Override
    public String getContentType(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Event image not found: " + id))
            .getImage()
            .getContentType();
    }
}
