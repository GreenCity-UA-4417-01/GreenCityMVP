package greencity.repository;

import greencity.entity.NewsletterSubscription;
import greencity.enums.EmailSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepo extends JpaRepository<NewsletterSubscription, Long> {
    /**
     * Method to find {@link NewsletterSubscriptionRepo} by email.
     *
     * @param email email.
     * @return {@link NewsletterSubscription} if exists.
     */
    Optional<NewsletterSubscription> findByEmail(String email);

    /**
     * Method to find all {@link NewsletterSubscriptionRepo} by status.
     *
     * @param status email.
     * @return list of {@link NewsletterSubscription}.
     */
    List<NewsletterSubscription> findAllByStatus(EmailSubscriptionStatus status);

    /**
     * Method to find if {@link NewsletterSubscriptionRepo} exists by email.
     *
     * @param email email.
     * @return Boolean.
     */
    Boolean existsByEmail(String email);
}
