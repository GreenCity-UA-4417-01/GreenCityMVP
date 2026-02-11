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

        if (images != null && images.stream().anyMatch(f -> f != null && !f.isEmpty())) {
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

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read image bytes from file: " + file.getOriginalFilename());
        }
    }
}
