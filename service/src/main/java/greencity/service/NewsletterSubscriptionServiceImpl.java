package greencity.service;

import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.EmailSubscriptionStatus;
import greencity.repository.NewsletterSubscriptionRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementation of NewsletterSubscription service.
 */
@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionServiceImpl implements NewsletterSubscriptionService {
    private final NewsletterSubscriptionRepo newsletterSubscriptionRepo;
    private final ModelMapper modelMapper;

    /**
     * Saves newsletter subscription by email. If subscription with given email
     * already exists, existing entity is returned. Otherwise, a new subscription
     * with {@link EmailSubscriptionStatus} status is created.
     *
     * @param email email address for subscription
     * @return {@link NewsletterSubscriptionResponseDto} of saved or existing
     *         subscription
     */
    @Override
    public NewsletterSubscriptionResponseDto save(String email) {
        NewsletterSubscription subscription = newsletterSubscriptionRepo
            .findByEmail(email)
            .orElseGet(() -> newsletterSubscriptionRepo.save(
                NewsletterSubscription.builder()
                    .email(email)
                    .status(EmailSubscriptionStatus.SUBSCRIBED)
                    .build()));

        return modelMapper.map(subscription, NewsletterSubscriptionResponseDto.class);
    }

    /**
     * Method for getting all emails that have
     * {@link EmailSubscriptionStatus#SUBSCRIBED} status.
     *
     * @return list of subscribed email addresses
     */
    @Override
    public List<String> getAllSubscribedEmails() {
        return newsletterSubscriptionRepo.findAllByStatus(EmailSubscriptionStatus.SUBSCRIBED).stream()
            .map(ent -> ent.getEmail()).toList();
    }

    /**
     * Method for deleting subscription entity by id.
     *
     * @param newsletterSubscriptionId id of subscription to delete
     * @throws EntityNotFoundException if subscription does not exist
     */
    @Override
    public void deleteById(Long newsletterSubscriptionId) {
        NewsletterSubscription subscription = newsletterSubscriptionRepo.findById(newsletterSubscriptionId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Newsletter subscription not found with id: " + newsletterSubscriptionId));

        newsletterSubscriptionRepo.deleteById(newsletterSubscriptionId);
    }

    /**
     * Checks whether subscription exists by email.
     *
     * @param email email address to check
     * @return true if subscription exists, false otherwise
     */
    @Override
    public boolean existsByEmail(String email) {
        return newsletterSubscriptionRepo.existsByEmail(email);
    }
}
