package greencity.dto.event;

import greencity.enums.InviteScope;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private Long id;

    private String title;
    private String description;

    private boolean open;
    private InviteScope inviteScope;

    private List<InitiativeTypeDto> initiativeTypes;
    private List<EventDateLocationDto> datesLocations;
    private List<EventImageDto> images;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
