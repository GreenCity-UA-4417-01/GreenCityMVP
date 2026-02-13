package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.newslettersubscription.NewsletterSubscriptionRequestDto;
import greencity.dto.newslettersubscription.NewsletterSubscriptionResponseDto;
import greencity.service.NewsletterSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/newsletter")
public class NewsletterSubscriptionController {
    private final NewsletterSubscriptionService newsletterSubscriptionService;

    /**
     * Method saves {@link NewsletterSubscriptionResponseDto} entity.
     *
     * @param requestDto contains email for subscription.
     * @return {@link NewsletterSubscriptionResponseDto}.
     */
    @Operation(summary = "Subscribe to eco-news newsletter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = NewsletterSubscriptionResponseDto.class))),
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = NewsletterSubscriptionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterSubscriptionResponseDto> saveSubscription(
        @Valid @RequestBody NewsletterSubscriptionRequestDto requestDto) {
        String email = requestDto.getEmail();

        boolean isExisting = newsletterSubscriptionService.existsByEmail(email);

        NewsletterSubscriptionResponseDto responseDto = newsletterSubscriptionService.save(email);

        HttpStatus respondStatusCode = isExisting ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(respondStatusCode).body(responseDto);
    }
}
