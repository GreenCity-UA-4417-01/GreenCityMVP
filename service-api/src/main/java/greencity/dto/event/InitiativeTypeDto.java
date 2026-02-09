package greencity.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeTypeDto {
    private String code;
    private String name;
}
