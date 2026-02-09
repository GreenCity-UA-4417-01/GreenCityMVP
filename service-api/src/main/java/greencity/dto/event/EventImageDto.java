package greencity.dto.event;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImageDto {
    private Long id;
    private boolean main;
    private String contentType;
    private String fileName;
    private OffsetDateTime createdAt;
}
