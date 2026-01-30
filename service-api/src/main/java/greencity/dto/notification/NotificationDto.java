package greencity.dto.notification;

import lombok.*;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String authorName;
    private String actionType;
    private String objectName;
    private ZonedDateTime creationDate;
    private boolean isRead;
}