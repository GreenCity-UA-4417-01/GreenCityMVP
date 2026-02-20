package greencity.controller.event;

import greencity.service.EventImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/images")
public class EventImageController {
    private final EventImageService eventImageService;

    @GetMapping("/content/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] bytes = eventImageService.getImageBytes(id);
        String contentType = eventImageService.getContentType(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(bytes);
    }
}
