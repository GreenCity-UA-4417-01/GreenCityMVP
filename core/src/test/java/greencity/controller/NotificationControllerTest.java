package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.NotificationService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationControllerTest {

    private static final String notificationsLink = "/notifications";

    private MockMvc mockMvc;

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    private Principal principal = getPrincipal();

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(notificationController)
            .setCustomArgumentResolvers(
                new UserArgumentResolver(userService, modelMapper))
            .setControllerAdvice(
                new CustomExceptionHandler(errorAttributes, objectMapper))
            .build();
    }

    @Test
    void getAllNotificationsTest() throws Exception {
        UserVO userVO = getUserVO();
        List<NotificationDto> notifications = Collections.emptyList();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(notificationService.getAllNotifications(userVO.getId(), null))
            .thenReturn(notifications);

        mockMvc.perform(get(notificationsLink)
            .principal(principal))
            .andExpect(status().isOk());

        verify(notificationService).getAllNotifications(userVO.getId(), null);
    }

    @Test
    void deleteNotificationTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(notificationsLink + "/{id}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService).deleteNotification(1L, userVO.getId());
    }

    @Test
    void deleteNotificationNotFoundTest() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        doThrow(NotFoundException.class)
            .when(notificationService)
            .deleteNotification(anyLong(), anyLong());

        mockMvc.perform(delete(notificationsLink + "/{id}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(notificationService).deleteNotification(1L, userVO.getId());
    }
}
