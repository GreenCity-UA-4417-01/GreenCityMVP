package greencity.controller.event;

import greencity.annotations.CurrentUser;
import greencity.annotations.EventImagesValidation;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDto;
import greencity.dto.user.UserVO;
import greencity.exception.handler.ExceptionResponse;
import greencity.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @Operation(
        summary = "Create an event",
        description = """
            Creates a new event using multipart/form-data:
            - JSON part: addEventDtoRequest
            - File part: images (optional, multiple)
            """)
    @ApiResponse(responseCode = "201", description = "Event created",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EventDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid data - validation error", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(
        responseCode = "413",
        description = "Payload too large - image exceeds maximum allowed size (10 MB)",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ExceptionResponse.class)))
    @PostMapping(
        value = "/create",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventDto> create(
        @Parameter(
            description = "Event create request (JSON part)",
            required = true) @RequestPart("addEventDtoRequest") @Valid AddEventDtoRequest request,
        @Parameter(
            description = "Images (optional, multiple). Allowed: JPG/PNG. Max 5 files.",
            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(value = "images",
                required = false) @EventImagesValidation List<MultipartFile> images,

        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        EventDto created = eventService.create(request, images, userVO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Delete an event",
        description = "Deletes an event by id. Allowed for ADMIN or event organizer.")
    @ApiResponse(responseCode = "204", description = "Event deleted", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden - only admin or organizer can delete",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        eventService.delete(id, userVO.getId());
        return ResponseEntity.noContent().build();
    }
}
