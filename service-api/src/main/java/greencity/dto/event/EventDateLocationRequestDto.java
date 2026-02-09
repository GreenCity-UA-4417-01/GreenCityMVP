package greencity.dto.event;

import greencity.annotations.ValidEventDateTimeRange;
import greencity.annotations.ValidEventLocation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidEventDateTimeRange
@ValidEventLocation
public class EventDateLocationRequestDto {
    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;
    private Boolean allDay;
    @Size(max = 255)
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Size(max = 2048)
    private String onlineLink;
}
