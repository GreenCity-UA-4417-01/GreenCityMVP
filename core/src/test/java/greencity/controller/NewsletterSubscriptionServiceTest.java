package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.newslettersubscription.NewsletterSubscriptionRequestDto;
import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.service.NewsletterSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class NewsletterSubscriptionServiceTest {
    private static final String NEWSLETTER_LINK = "/newsletter";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @InjectMocks
    private NewsletterSubscriptionController newsletterSubscriptionController;

    @Mock
    private NewsletterSubscriptionService newsletterSubscriptionService;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        this.mockMvc = MockMvcBuilders
            .standaloneSetup(newsletterSubscriptionController)
            .setValidator(new LocalValidatorFactoryBean())
            .build();
    }

    @Test
    void saveSubscription() throws Exception {
        String email = "test@mail.com";
        NewsletterSubscriptionRequestDto newsletterSubscriptionRequestDto =
            new NewsletterSubscriptionRequestDto().setEmail(email);
        String jsonMapper = objectMapper.writeValueAsString(newsletterSubscriptionRequestDto);

        when(newsletterSubscriptionService.existsByEmail(email)).thenReturn(false);
        when(newsletterSubscriptionService.save(email))
            .thenReturn(new NewsletterSubscriptionResponseDto().setEmail(email));

        mockMvc.perform(post(NEWSLETTER_LINK + "/subscribe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper))
            .andExpect(status().isCreated());

        verify(newsletterSubscriptionService).save(email);
    }

    @Test
    void saveSubscriptionBadRequest() throws Exception {
        String email = "testmail.com";
        NewsletterSubscriptionRequestDto newsletterSubscriptionRequestDto =
            new NewsletterSubscriptionRequestDto().setEmail(email);
        String jsonMapper = objectMapper.writeValueAsString(newsletterSubscriptionRequestDto);

        mockMvc.perform(post(NEWSLETTER_LINK + "/subscribe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper))
            .andExpect(status().isBadRequest());
    }
}
