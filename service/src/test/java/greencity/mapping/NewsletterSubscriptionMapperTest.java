package greencity.mapping;

import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.EmailSubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class NewsletterSubscriptionMapperTest {

    private final NewsletterSubscriptionMapper mapper = new NewsletterSubscriptionMapper();

    @Test
    void convertTest_ShouldMapAllFields() {
        NewsletterSubscription newsletterSubscription = NewsletterSubscription.builder()
            .id(1L)
            .email("test@mail.com")
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        NewsletterSubscriptionResponseDto responseDto = mapper.convert(newsletterSubscription);

        assertNotNull(responseDto);
        assertEquals(newsletterSubscription.getId(), responseDto.getId());
        assertEquals(newsletterSubscription.getEmail(), responseDto.getEmail());
        assertEquals(newsletterSubscription.getStatus(), responseDto.getStatus());
    }
}
