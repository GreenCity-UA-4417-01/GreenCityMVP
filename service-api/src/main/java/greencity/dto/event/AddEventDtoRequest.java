package greencity.dto.event;

import greencity.enums.InviteScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEventDtoRequest {
    @NotBlank
    @Size(max = 70)
    private String title;

    @NotBlank
    @Size(min = 20, max = 63206)
    private String description;

    private Boolean open;

    private InviteScope inviteScope;

    @NotEmpty
    private List<@NotBlank String> initiativeTypes;

    @Valid
    @NotEmpty
    @Size(min = 1, max = 7)
    private List<EventDateLocationRequestDto> datesLocations;
}
