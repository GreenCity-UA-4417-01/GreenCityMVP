package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.exception.handler.ExceptionResponse;
import greencity.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications(@CurrentUser UserVO userVO) {
        return ResponseEntity.ok(notificationService.getAllNotifications(userVO.getId()));
    }

    @Operation(summary = "Delete a notification by its ID for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found or doesn't belong to user",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @CurrentUser UserVO userVO) {
        notificationService.deleteNotification(id, userVO.getId());
        return ResponseEntity.ok().build();
    }
}