package greencity.service;

import greencity.dto.notification.NotificationDto;
import java.util.List;

public interface NotificationService {
    List<NotificationDto> getAllNotifications(Long userId);

    void deleteNotification(Long notificationId, Long userId);
}