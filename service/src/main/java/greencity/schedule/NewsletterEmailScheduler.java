package greencity.schedule;

import greencity.client.RestClient;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.notification.EmailNotificationDto;
import greencity.service.EcoNewsService;
import greencity.service.NewsletterSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * Scheduler, that is responsible for sending interesting news to subscribed
 * emails.
 */
@Component()
@Slf4j
@RequiredArgsConstructor
public class NewsletterEmailScheduler {
    private final RestClient restClient;
    private final NewsletterSubscriptionService newsletterSubscriptionService;
    private final EcoNewsService ecoNewsService;

    /**
     * This schedule used for sending {@link EmailNotificationDto} with interesting
     * news to subscribed emails.
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyNewsletter() {
        EcoNewsDto theMostLikedEcoNews = ecoNewsService.getTheMostLikedEcoNews();

        EmailNotificationDto dto = EmailNotificationDto.builder()
            .title(theMostLikedEcoNews.getTitle())
            .body(theMostLikedEcoNews.getContent())
            .build();

        List<String> listOfSubscribedEmails = newsletterSubscriptionService.getAllSubscribedEmails();

        for (String email : listOfSubscribedEmails) {
            try {
                restClient.sendUserNotification(dto, email);
            } catch (Exception e) {
                log.error("Newsletter scheduler failed", e);
            }
        }
    }
}
