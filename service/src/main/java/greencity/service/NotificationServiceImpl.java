package greencity.service;

import greencity.dto.notification.NotificationDto;
import greencity.entity.Notification;
import greencity.enums.NotificationSource;
import greencity.exception.exceptions.NotDeletedException;
import greencity.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepo notificationRepo;

    @Override
    public List<NotificationDto> getAllNotifications(Long userId, NotificationSource source) {
        List<Notification> notifications = (source == null)
            ? notificationRepo.findAllByReceiverIdOrderByCreationDateDesc(userId)
            : notificationRepo.findAllByReceiverIdAndSourceOrderByCreationDateDesc(userId, source);

        return notifications.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .authorName(notification.getAuthor().getName())
            .actionType(notification.getActionType())
            .objectName(notification.getObjectName())
            .source(notification.getSource()) // Добавляем наше новое поле!
            .creationDate(notification.getCreationDate())
            .isRead(notification.isRead())
            .build();
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        int deletedCount = notificationRepo.deleteByIdAndReceiverId(notificationId, userId);
        if (deletedCount == 0) {
            throw new NotDeletedException("Notification not found or doesn't belong to user");
        }
    }
}