package greencity.service.impl;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDateLocationRequestDto;
import greencity.dto.event.EventDto;
import greencity.entity.User;
import greencity.entity.event.*;
import greencity.enums.InviteScope;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.EventDtoMapper;
import greencity.repository.UserRepo;
import greencity.repository.event.EventRepository;
import greencity.repository.event.InitiativeTypeRepository;
import greencity.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final EventRepository eventRepository;
    private final InitiativeTypeRepository initiativeTypeRepository;
    private final UserRepo userRepository;
    private final EventDtoMapper eventDtoMapper;

    @Override
    @Transactional
    public EventDto create(AddEventDtoRequest request, List<MultipartFile> images, Long organizerId) {
        if (organizerId == null) {
            throw new BadRequestException("User must be authenticated to create an event");
        }

        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new NotFoundException("Event organizer not found with id: " + organizerId));

        boolean open = request.getOpen() == null || request.getOpen();
        InviteScope inviteScope = request.getInviteScope() == null ? InviteScope.ALL : request.getInviteScope();

        validateInviteScope(open, inviteScope);
        Set<InitiativeType> initiativeTypes = resolveInitiativeTypes(request.getInitiativeTypes());

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setOpen(open);
        event.setInviteScope(inviteScope);
        event.setInitiativeTypes(initiativeTypes);

        attachDateTimeLocations(event, request.getDatesLocations());

        if (images != null && !images.isEmpty()) {
            attachImages(event, images);
        }

        Event saved = eventRepository.save(event);
        return eventDtoMapper.toDto(saved);
    }

    private void validateInviteScope(boolean open, InviteScope inviteScope) {
        if (!open && inviteScope != InviteScope.FRIENDS) {
            throw new BadRequestException(
                "Closed events must have inviteScope set to FRIENDS. Current inviteScope: " + inviteScope);
        }
    }

    private Set<InitiativeType> resolveInitiativeTypes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new BadRequestException("Initiative types are required");
        }

        List<InitiativeType> found = initiativeTypeRepository.findByCodeIn(codes);
        Set<String> uniqueCodes = new HashSet<>(codes);

        if (found.size() != uniqueCodes.size()) {
            Set<String> foundCodes = found.stream()
                .map(InitiativeType::getCode)
                .collect(Collectors.toSet());

            Set<String> missingCodes = new HashSet<>(uniqueCodes);
            missingCodes.removeAll(foundCodes);

            throw new BadRequestException("Unknown initiative type codes: " + missingCodes);
        }

        return new HashSet<>(found);
    }

    private void attachDateTimeLocations(Event event, List<EventDateLocationRequestDto> items) {
        for (EventDateLocationRequestDto dto : items) {
            EventDateTimeLocation location = new EventDateTimeLocation();
            location.setEvent(event);
            location.setStartDateTime(dto.getStartDateTime());
            location.setEndDateTime(dto.getEndDateTime());
            location.setAllDay(Boolean.TRUE.equals(dto.getAllDay()));
            location.setLocationName(dto.getLocationName());
            location.setLatitude(dto.getLatitude());
            location.setLongitude(dto.getLongitude());
            location.setOnlineLink(dto.getOnlineLink());

            event.getDateTimeLocations().add(location);
        }
    }

    private void attachImages(Event event, List<MultipartFile> images) {
        validateImages(images);

        boolean mainAssigned = false;

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            EventImage image = new EventImage();
            image.setEvent(event);
            image.setContentType(file.getContentType());
            image.setFileName(file.getOriginalFilename());
            image.setCreatedAt(OffsetDateTime.now());

            if (!mainAssigned) {
                image.setMain(true);
                mainAssigned = true;
            } else {
                image.setMain(false);
            }

            EventImageData data = new EventImageData();
            data.setImage(image);
            data.setData(readBytes(file));
            image.setData(data);

            event.getImages().add(image);
        }
    }

    private void validateImages(List<MultipartFile> images) {
        long nonEmptyCount = images.stream()
            .filter(file -> file != null && !file.isEmpty())
            .count();

        if (nonEmptyCount > MAX_IMAGES) {
            throw new BadRequestException(
                "Maximum allowed images count is " + MAX_IMAGES + ". Provided: " + nonEmptyCount);
        }

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new BadRequestException(
                    "Unsupported image content type: " + contentType + ". Allowed formats: JPG, PNG");
            }

            if (file.getSize() > MAX_IMAGE_BYTES) {
                throw new BadRequestException(
                    "Incorrect image size. Maximum allowed size is 10 MB. Image '"
                        + file.getOriginalFilename() + "' is " + (file.getSize() / 1024 / 1024) + " MB");
            }
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read image bytes from file: " + file.getOriginalFilename());
        }
    }
}
