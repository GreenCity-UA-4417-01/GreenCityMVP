package greencity.dto.newslettersubscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import greencity.enums.EmailSubscriptionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class NewsletterSubscriptionResponseDto {
    private Long id;

    @NotEmpty
    @Email
    private String email;

    @NotNull
    private EmailSubscriptionStatus status;
}