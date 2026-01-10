package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.econewscomment.AddEcoNewsCommentDtoRequest;
import greencity.dto.habit.*;
import greencity.dto.user.UserVO;
import greencity.entity.HabitAssign;
import greencity.enums.HabitAssignStatus;
import greencity.service.HabitAssignService;
import greencity.service.HabitService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import greencity.config.SecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.quality.Strictness.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
public class HabitAssignControllerTest {
    private static final String HabitAssignControllerLink= "/habit/assign";
    private MockMvc mockMvc;

    @InjectMocks
    private HabitAssignController habitAssignController;

    @Mock
    private HabitAssignService habitAssignService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;


    private Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper)
                )
                .build();
    }

    @Test
    void assignDefault() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;

        HabitAssignManagementDto response = new HabitAssignManagementDto();

        when(habitAssignService.assignDefaultHabitForUser(
                habitId, userVO))
                .thenReturn(response);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}", habitId)
                        .principal(principal))
                .andExpect(status().isCreated());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    void assignCustom() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();
        HabitAssignCustomPropertiesDto requestDto = new HabitAssignCustomPropertiesDto();

        List<HabitAssignManagementDto> response =
                List.of(new HabitAssignManagementDto());

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.assignCustomHabitForUser(
                habitId, userVO, requestDto))
                .thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}/custom", habitId)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).assignCustomHabitForUser(habitId, userVO, requestDto);
    }

    @Test
    void updateHabitAssignDuration()  throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Integer duration = 1;
        HabitAssignUserDurationDto response = new HabitAssignUserDurationDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration)).thenReturn(response);

        mockMvc.perform(put(HabitAssignControllerLink + "/{habitAssignId}/update-habit-duration", habitAssignId)
                .principal(principal)
                .param("duration", duration.toString()))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration);
    }

    @Test
    void getHabitAssign() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        HabitAssignDto response = new HabitAssignDto();
        Locale locale = new Locale("en");

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}", habitAssignId)
                .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage());
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquired() throws Exception {
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        List<HabitAssignDto> response = List.of(new HabitAssignDto());

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/allForCurrentUser")
                .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage());
    }

    @Test
    void getUserShoppingAndCustomShoppingLists() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        UserShoppingAndCustomShoppingListsDto response = new UserShoppingAndCustomShoppingListsDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getUserShoppingAndCustomShoppingLists(userVO.getId(), habitAssignId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
                .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(userVO.getId(), habitAssignId, locale.getLanguage());
    }

    @Test
    void updateUserAndCustomShoppingLists() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");

        UserShoppingAndCustomShoppingListsDto requestDto = new UserShoppingAndCustomShoppingListsDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(put(HabitAssignControllerLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
                .principal(principal)
                .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(userVO.getId(), habitAssignId, requestDto, locale.getLanguage());
    }

    @Test
    void getListOfUserAndCustomShoppingListsInprogress() throws Exception {
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        List<UserShoppingAndCustomShoppingListsDto> response = List.of(new UserShoppingAndCustomShoppingListsDto());

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/allUserAndCustomShoppingListsInprogress")
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getListOfUserAndCustomShoppingListsWithStatusInprogress(userVO.getId(), locale.getLanguage());
    }

    @Test
    void getAllHabitAssignsByHabitIdAndAcquired() throws Exception {
        Long habitId = 1L;
        Locale locale = new Locale("en");
        List<HabitAssignDto> response = List.of(new HabitAssignDto());

        when(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitId}/all", habitId)
                .principal(principal)
                .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(habitAssignService).getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, locale.getLanguage());
    }

    @Test
    void getHabitAssignByHabitId() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;
        Locale locale = new Locale("en");
        HabitAssignDto response = new HabitAssignDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitId}/active", habitId)
                .principal(principal)
                .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage());
    }

    @Test
    void getUsersHabitByHabitAssignId() throws Exception {
        UserVO userVO = getUserVO();
        Long habitAssignId = 1L;
        Locale locale = new Locale("en");
        HabitDto response = new HabitDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}/more", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage());
    }

    @Test
    void updateAssignByHabitId() throws Exception {
        Long habitAssignId = 1L;

        HabitAssignStatDto requestDto = new HabitAssignStatDto();
        HabitAssignManagementDto response = new HabitAssignManagementDto();

        requestDto.setStatus(HabitAssignStatus.INPROGRESS);

        when(habitAssignService.updateStatusByHabitAssignId(habitAssignId, requestDto)).thenReturn(response);

        mockMvc.perform(patch(HabitAssignControllerLink + "/{habitAssignId}", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(habitAssignService).updateStatusByHabitAssignId(habitAssignId, requestDto);
    }

    @Test
    void enrollHabit() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        LocalDate date = LocalDate.of(2026, 1, 10);

        HabitAssignDto response = new HabitAssignDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitAssignId}/enroll/{date}", habitAssignId, date)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage());
    }

    @Test
    void unenrollHabit() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        LocalDate date = LocalDate.of(2026, 1, 10);

        HabitAssignDto response = new HabitAssignDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.unenrollHabit(habitAssignId, userVO.getId(), date)).thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitAssignId}/unenroll/{date}", habitAssignId, date)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).unenrollHabit(habitAssignId, userVO.getId(), date);
    }

    @Test
    void getInprogressHabitAssignOnDate() throws Exception {
        UserVO userVO = getUserVO();
        LocalDate date = LocalDate.of(2026, 1, 10);
        Locale locale = new Locale("en");

        List<HabitAssignDto> response = List.of(new HabitAssignDto());

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/active/{date}", date)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage());
    }

    @Test
    void getHabitAssignBetweenDates() throws Exception {
        UserVO userVO = getUserVO();
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 6, 10);
        Locale locale = new Locale("en");

        List<HabitsDateEnrollmentDto> response = List.of(new HabitsDateEnrollmentDto());

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/activity/{from}/to/{to}", from, to)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage());
    }

    @Test
    void cancelHabitAssign() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();

        HabitAssignDto response = new HabitAssignDto();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.cancelHabitAssign(habitId, userVO.getId())).thenReturn(response);

        mockMvc.perform(patch(HabitAssignControllerLink + "/cancel/{habitId}", habitId)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).cancelHabitAssign(habitId, userVO.getId());
    }

    @Test
    void deleteHabitAssign() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(delete(HabitAssignControllerLink + "/delete/{habitAssignId}", habitAssignId)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).deleteHabitAssign(habitAssignId, userVO.getId());
    }

    @Test
    void updateShoppingListStatus() throws Exception {
        UpdateUserShoppingListDto requestDto = new UpdateUserShoppingListDto();

        mockMvc.perform(put(HabitAssignControllerLink + "/saveShoppingListForHabitAssign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(habitAssignService).updateUserShoppingListItem(requestDto);
    }

    @Test
    void updateProgressNotificationHasDisplayed() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(put(HabitAssignControllerLink + "/{habitAssignId}/updateProgressNotificationHasDisplayed", habitAssignId)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitAssignService).updateProgressNotificationHasDisplayed(habitAssignId, userVO.getId());
    }

}
