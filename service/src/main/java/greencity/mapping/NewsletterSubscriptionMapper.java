package greencity.mapping;

import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.entity.NewsletterSubscription;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that is used by {@link ModelMapper} to map NewsletterSubscription into
 * NewsletterSubscriptionResponseDto.
 */
@Component
public class NewsletterSubscriptionMapper
    extends AbstractConverter<NewsletterSubscription, NewsletterSubscriptionResponseDto> {
    /**
     * Method convert {@link NewsletterSubscription} to
     * {@link NewsletterSubscriptionResponseDto}.
     *
     * @return {@link NewsletterSubscriptionResponseDto}
     */
    @Override
    protected NewsletterSubscriptionResponseDto convert(NewsletterSubscription newsletterSubscription) {
        if (newsletterSubscription == null) {
            return null;
        }

        return NewsletterSubscriptionResponseDto.builder()
            .id(newsletterSubscription.getId())
            .email(newsletterSubscription.getEmail())
            .status(newsletterSubscription.getStatus())
            .build();
    }
}
