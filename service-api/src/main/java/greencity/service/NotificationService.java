package greencity.service;

import greencity.dto.notification.NotificationDto;
import java.util.List;
import greencity.enums.NotificationSource;

public interface NotificationService {
    List<NotificationDto> getAllNotifications(Long userId, NotificationSource source);

    void deleteNotification(Long notificationId, Long userId);
}