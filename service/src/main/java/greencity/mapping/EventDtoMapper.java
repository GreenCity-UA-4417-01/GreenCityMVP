package greencity.mapping;

import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.EventImageDto;
import greencity.dto.event.InitiativeTypeDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateTimeLocation;
import greencity.entity.event.EventImage;
import greencity.entity.event.InitiativeType;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class EventDtoMapper {
    public EventDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .open(event.isOpen())
            .inviteScope(event.getInviteScope())
            .initiativeTypes(toInitiativeTypeDtos(event.getInitiativeTypes()))
            .datesLocations(toDateLocationDtos(event.getDateTimeLocations()))
            .images(toImageDtos(event.getImages()))
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .build();
    }

    private List<InitiativeTypeDto> toInitiativeTypeDtos(Set<InitiativeType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }

        return types.stream()
            .sorted(Comparator.comparing(InitiativeType::getCode))
            .map(this::toInitiativeTypeDto)
            .toList();
    }

    private InitiativeTypeDto toInitiativeTypeDto(InitiativeType type) {
        return InitiativeTypeDto.builder()
            .code(type.getCode())
            .name(type.getName())
            .build();
    }

    private List<EventDateLocationDto> toDateLocationDtos(Set<EventDateTimeLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of();
        }

        return locations.stream()
            .sorted(Comparator.comparing(EventDateTimeLocation::getStartDateTime))
            .map(this::toDateLocationDto)
            .toList();
    }

    private EventDateLocationDto toDateLocationDto(EventDateTimeLocation l) {
        return EventDateLocationDto.builder()
            .startDateTime(l.getStartDateTime())
            .endDateTime(l.getEndDateTime())
            .allDay(l.isAllDay())
            .locationName(l.getLocationName())
            .latitude(l.getLatitude())
            .longitude(l.getLongitude())
            .onlineLink(l.getOnlineLink())
            .build();
    }

    private List<EventImageDto> toImageDtos(List<EventImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
            .sorted(Comparator
                .comparing(EventImage::isMain).reversed()
                .thenComparing(EventImage::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(EventImage::getId,
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .map(this::toImageDto)
            .toList();
    }

    private EventImageDto toImageDto(EventImage img) {
        return EventImageDto.builder()
            .id(img.getId())
            .main(img.isMain())
            .contentType(img.getContentType())
            .fileName(img.getFileName())
            .createdAt(img.getCreatedAt())
            .build();
    }
}
