package greencity.dto.newslettersubscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class NewsletterSubscriptionRequestDto {
    @NotEmpty
    @Email
    private String email;
}