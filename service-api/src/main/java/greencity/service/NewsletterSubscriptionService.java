package greencity.service;

import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import java.util.List;

public interface NewsletterSubscriptionService {
    /**
     * Method to save newsletter subscription.
     *
     * @param email user's email.
     * @return NewsletterSubscriptionResponseDto.
     */
    NewsletterSubscriptionResponseDto save(String email);

    /**
     * Method for getting all emails witch subscription.
     *
     * @return a list of subscribed email addresses; never {@code null}
     */
    List<String> getAllSubscribedEmails();

    /**
     * Method for deleting newsletter subscription by id.
     *
     * @param newsletterSubscriptionId newsletters subscription's id.
     */
    void deleteById(Long newsletterSubscriptionId);

    /**
     * Checks if an email is already subscribed.
     *
     * @param email email to check
     * @return true if email already exists, false otherwise
     */
    boolean existsByEmail(String email);
}
