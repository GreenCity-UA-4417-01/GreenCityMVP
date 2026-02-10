package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDto;
import greencity.dto.user.UserVO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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
    @PostMapping(
        value = "/create",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventDto> create(
            @Parameter(
                description = "Event create request (JSON part)",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(
                        implementation = AddEventDtoRequest.class))) @RequestPart("addEventDtoRequest") @Valid AddEventDtoRequest request,
        @Parameter(
            description = "Images (optional, multiple). Allowed: JPG/PNG. Max 5 files.",
            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(value = "images",
                required = false) List<MultipartFile> images,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        EventDto created = eventService.create(request, images, userVO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
