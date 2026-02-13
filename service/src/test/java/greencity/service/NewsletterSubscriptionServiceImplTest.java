package greencity.service;

import greencity.ModelUtils;
import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.entity.NewsletterSubscription;
import greencity.enums.EmailSubscriptionStatus;
import greencity.repository.NewsletterSubscriptionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class NewsletterSubscriptionServiceImplTest {

    @Mock
    NewsletterSubscriptionRepo newsletterSubscriptionRepo;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    private NewsletterSubscriptionServiceImpl service;

    private NewsletterSubscription subscription = ModelUtils.getNewsletterSubscription();

    @Test
    void save() {
        String email = "test@email.com";

        NewsletterSubscription subscription = NewsletterSubscription.builder()
            .id(1L)
            .email(email)
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        NewsletterSubscriptionResponseDto dto = NewsletterSubscriptionResponseDto.builder()
            .id(1L)
            .email(email)
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        when(newsletterSubscriptionRepo.findByEmail(email)).thenReturn(Optional.of(subscription));
        when(modelMapper.map(subscription, NewsletterSubscriptionResponseDto.class)).thenReturn(dto);

        NewsletterSubscriptionResponseDto result = service.save(email);

        assertEquals(dto, result);
        verify(newsletterSubscriptionRepo, never()).save(any());
    }

    @Test
    void save_shouldCreateNewSubscription() {
        String email = "test@email.com";

        NewsletterSubscription subscription = NewsletterSubscription.builder()
            .id(1L)
            .email(email)
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        NewsletterSubscriptionResponseDto dto = NewsletterSubscriptionResponseDto.builder()
            .id(1L)
            .email(email)
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        when(newsletterSubscriptionRepo.findByEmail(email)).thenReturn(Optional.empty());

        when(newsletterSubscriptionRepo.save(any())).thenReturn(subscription);

        when(modelMapper.map(subscription, NewsletterSubscriptionResponseDto.class)).thenReturn(dto);

        NewsletterSubscriptionResponseDto result = service.save(email);

        assertEquals(dto, result);
        verify(newsletterSubscriptionRepo).save(any());
    }

    @Test
    void getAllSubscribedEmails_shouldReturnEmailsList() {
        NewsletterSubscription sub1 = NewsletterSubscription.builder()
            .email("tasts1@email.com")
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        NewsletterSubscription sub2 = NewsletterSubscription.builder()
            .email("test2@email.com")
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        when(newsletterSubscriptionRepo.findAllByStatus(EmailSubscriptionStatus.SUBSCRIBED))
            .thenReturn(List.of(sub1, sub2));

        List<String> result = service.getAllSubscribedEmails();

        assertEquals(2, result.size());
    }

    @Test
    void deleteById_whenExists_ShouldDelete() {
        Long id = 1L;

        when(newsletterSubscriptionRepo.findById(id)).thenReturn(Optional.of(new NewsletterSubscription()));

        service.deleteById(id);

        verify(newsletterSubscriptionRepo).deleteById(id);
    }

    @Test
    void existsByEmail() {
        String email = "test@email.com";

        NewsletterSubscription subscription = NewsletterSubscription.builder()
            .id(1L)
            .email(email)
            .status(EmailSubscriptionStatus.SUBSCRIBED)
            .build();

        when(newsletterSubscriptionRepo.existsByEmail(any())).thenReturn(true);

        boolean result = service.existsByEmail(email);

        assertTrue(result);
        verify(newsletterSubscriptionRepo).existsByEmail(any());
    }
}
