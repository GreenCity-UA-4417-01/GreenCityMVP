package greencity.dto.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDateLocationDto {
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private boolean allDay;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String onlineLink;
}
