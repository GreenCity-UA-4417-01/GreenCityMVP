package greencity.service;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface EventService {
    EventDto create(AddEventDtoRequest request, List<MultipartFile> images, Long organizerId);
}
