package greencity.service;

import greencity.dto.notification.NotificationDto;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.exception.exceptions.NotDeletedException;
import greencity.repository.NotificationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getAllNotificationsShouldReturnListOfDTOsTest() {
        Long userId = 1L;

        User author = new User();
        author.setName("Test Author");

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setAuthor(author);
        notification.setActionType("LIKE");
        notification.setObjectName("EcoNews");
        notification.setCreationDate(ZonedDateTime.now());
        notification.setRead(false);

        when(notificationRepo.findAllByReceiverIdOrderByCreationDateDesc(userId))
            .thenReturn(List.of(notification));

        List<NotificationDto> result = notificationService.getAllNotifications(userId, null);

        assertEquals(1, result.size());

        NotificationDto dto = result.get(0);
        assertEquals(10L, dto.getId());
        assertEquals("Test Author", dto.getAuthorName());
        assertEquals("LIKE", dto.getActionType());
        assertEquals("EcoNews", dto.getObjectName());
        assertFalse(dto.isRead());

        verify(notificationRepo, times(1))
            .findAllByReceiverIdOrderByCreationDateDesc(userId);
    }

    @Test
    void getAllNotificationsShouldReturnEmptyListTest() {
        Long userId = 1L;

        when(notificationRepo.findAllByReceiverIdOrderByCreationDateDesc(userId))
            .thenReturn(Collections.emptyList());

        List<NotificationDto> result = notificationService.getAllNotifications(userId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(notificationRepo, times(1))
            .findAllByReceiverIdOrderByCreationDateDesc(userId);
    }

    @Test
    void deleteNotificationShouldDeleteSuccessfullyTest() {
        Long notificationId = 5L;
        Long userId = 1L;

        when(notificationRepo.deleteByIdAndReceiverId(notificationId, userId))
            .thenReturn(1);

        assertDoesNotThrow(() -> notificationService.deleteNotification(notificationId, userId));

        verify(notificationRepo, times(1))
            .deleteByIdAndReceiverId(notificationId, userId);
    }

    @Test
    void deleteNotificationShouldThrowNotDeletedExceptionTest() {
        Long notificationId = 5L;
        Long userId = 1L;

        when(notificationRepo.deleteByIdAndReceiverId(notificationId, userId))
            .thenReturn(0);

        assertThrows(NotDeletedException.class, () -> notificationService.deleteNotification(notificationId, userId));

        verify(notificationRepo, times(1))
            .deleteByIdAndReceiverId(notificationId, userId);
    }
}
