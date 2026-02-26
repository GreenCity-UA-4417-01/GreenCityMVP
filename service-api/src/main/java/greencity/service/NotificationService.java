package greencity.service;

import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import java.util.List;

public interface NotificationService {
    List<NotificationDto> getAllNotifications(Long userId);

    void createNotification(UserVO receiver, UserVO author, String objectName, String actionType);

    void deleteNotification(Long notificationId, Long userId);
}