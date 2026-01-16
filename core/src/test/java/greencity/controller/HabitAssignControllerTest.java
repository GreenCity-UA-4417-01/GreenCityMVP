package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habit.*;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import greencity.config.SecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private static ObjectMapper objectMapper;

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    private final Principal principal = getPrincipal();

    @BeforeAll
    static void initObjectMapper() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper)
                )
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void assignDefault() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;

        HabitAssignManagementDto response = HabitAssignManagementDto.builder()
                .id(10L)
                .status(HabitAssignStatus.INPROGRESS)
                .createDateTime(ZonedDateTime.now())
                .habitId(habitId)
                .userId(5L)
                .duration(21)
                .workingDays(5)
                .habitStreak(0)
                .lastEnrollment(ZonedDateTime.now())
                .progressNotificationHasDisplayed(false)
                .build();

        when(habitAssignService.assignDefaultHabitForUser(
                habitId, userVO))
                .thenReturn(response);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}", habitId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("INPROGRESS"))
                .andExpect(jsonPath("$.habitId").value(1L))
                .andExpect(jsonPath("$.userId").value(5L))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(0))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false));


        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    void assignDefault_shouldReturnNotFound_whenHabitNotExists() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 99L;

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        when(habitAssignService.assignDefaultHabitForUser(habitId, userVO))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}", habitId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    void assignDefault_shouldReturnBadRequest_whenHabitAlreadyAssigned() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        when(habitAssignService.assignDefaultHabitForUser(habitId, userVO))
                .thenThrow(BadRequestException.class);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}", habitId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    void assignCustom() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();

        HabitAssignPropertiesDto propertiesDto = HabitAssignPropertiesDto.builder()
                .duration(21)
                .defaultShoppingListItems(List.of(1L, 2L, 3L))
                .build();

        HabitAssignCustomPropertiesDto requestDto =
                HabitAssignCustomPropertiesDto.builder()
                        .habitAssignPropertiesDto(propertiesDto)
                        .friendsIdsList(List.of(5L, 6L))
                        .build();

        HabitAssignManagementDto responseDto = HabitAssignManagementDto.builder()
                .id(10L)
                .status(HabitAssignStatus.INPROGRESS)
                .createDateTime(ZonedDateTime.now())
                .habitId(habitId)
                .userId(5L)
                .duration(21)
                .workingDays(5)
                .habitStreak(0)
                .lastEnrollment(ZonedDateTime.now())
                .progressNotificationHasDisplayed(false)
                .build();

        List<HabitAssignManagementDto> response = List.of(responseDto);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.assignCustomHabitForUser(
                habitId, userVO, requestDto))
                .thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitId}/custom", habitId)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("INPROGRESS"))
                .andExpect(jsonPath("$[0].habitId").value(1))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].duration").value(21))
                .andExpect(jsonPath("$[0].workingDays").value(5))
                .andExpect(jsonPath("$[0].habitStreak").value(0));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).assignCustomHabitForUser(habitId, userVO, requestDto);
    }

    @Test
    void updateHabitAssignDuration()  throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Integer duration = 1;
        HabitAssignUserDurationDto response = HabitAssignUserDurationDto.builder()
                .habitAssignId(habitAssignId)
                .userId(userVO.getId())
                .habitId(10L)
                .status(HabitAssignStatus.INPROGRESS)
                .workingDays(3)
                .duration(duration)
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration)).thenReturn(response);

        mockMvc.perform(put(HabitAssignControllerLink + "/{habitAssignId}/update-habit-duration", habitAssignId)
                        .principal(principal)
                        .param("duration", duration.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habitAssignId").value(1))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.duration").value(1))
                .andExpect(jsonPath("$.status").value("INPROGRESS"));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration);
    }

    @Test
    void getHabitAssign() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        HabitAssignDto response = HabitAssignDto.builder()
                .id(habitAssignId)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(3)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .build();
        Locale locale = new Locale("en");

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}", habitAssignId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.status").value("INPROGRESS"));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage());
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquired() throws Exception {
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        Long habitAssignId = 1L;
        HabitAssignDto responseDto = HabitAssignDto.builder()
                .id(habitAssignId)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(3)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .build();

        List<HabitAssignDto> response = List.of(responseDto);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/allForCurrentUser")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(habitAssignId))
                .andExpect(jsonPath("$[0].userId").value(userVO.getId()))
                .andExpect(jsonPath("$[0].duration").value(21))
                .andExpect(jsonPath("$[0].workingDays").value(5))
                .andExpect(jsonPath("$[0].habitStreak").value(3))
                .andExpect(jsonPath("$[0].status").value("INPROGRESS"))
                .andExpect(jsonPath("$[0].progressNotificationHasDisplayed").value(false));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage());
    }

    @Test
    void getUserShoppingAndCustomShoppingLists() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        UserShoppingListItemResponseDto userItem = UserShoppingListItemResponseDto.builder()
                .id(1L)
                .text("User shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        CustomShoppingListItemResponseDto customItem = CustomShoppingListItemResponseDto.builder()
                .id(10L)
                .text("Custom shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        UserShoppingAndCustomShoppingListsDto response = UserShoppingAndCustomShoppingListsDto.builder()
                .userShoppingListItemDto(List.of(userItem))
                .customShoppingListItemDto(List.of(customItem))
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getUserShoppingAndCustomShoppingLists(userVO.getId(), habitAssignId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userShoppingListItemDto[0].id").value(1))
                .andExpect(jsonPath("$.userShoppingListItemDto[0].text").value("User shopping item"))
                .andExpect(jsonPath("$.userShoppingListItemDto[0].status").value("DONE"))
                .andExpect(jsonPath("$.customShoppingListItemDto[0].id").value(10))
                .andExpect(jsonPath("$.customShoppingListItemDto[0].text").value("Custom shopping item"))
                .andExpect(jsonPath("$.customShoppingListItemDto[0].status").value("DONE"));


        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(userVO.getId(), habitAssignId, locale.getLanguage());
    }

    @Test
    void updateUserAndCustomShoppingLists() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");

        UserShoppingListItemResponseDto userItem = UserShoppingListItemResponseDto.builder()
                .id(1L)
                .text("User shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        CustomShoppingListItemResponseDto customItem = CustomShoppingListItemResponseDto.builder()
                .id(10L)
                .text("Custom shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        UserShoppingAndCustomShoppingListsDto requestDto = UserShoppingAndCustomShoppingListsDto.builder()
                .userShoppingListItemDto(List.of(userItem))
                .customShoppingListItemDto(List.of(customItem))
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(put(HabitAssignControllerLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(userVO.getId(), habitAssignId, requestDto, locale.getLanguage());
    }

    @Test
    void getListOfUserAndCustomShoppingListsInprogress() throws Exception {
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        UserShoppingListItemResponseDto userItem = UserShoppingListItemResponseDto.builder()
                .id(1L)
                .text("User shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        CustomShoppingListItemResponseDto customItem = CustomShoppingListItemResponseDto.builder()
                .id(10L)
                .text("Custom shopping item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        UserShoppingAndCustomShoppingListsDto listDto = UserShoppingAndCustomShoppingListsDto.builder()
                .userShoppingListItemDto(List.of(userItem))
                .customShoppingListItemDto(List.of(customItem))
                .build();

        List<UserShoppingAndCustomShoppingListsDto> response = List.of(listDto);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(userVO.getId(), locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/allUserAndCustomShoppingListsInprogress")
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userShoppingListItemDto[0].id").value(1))
                .andExpect(jsonPath("$[0].userShoppingListItemDto[0].text").value("User shopping item"))
                .andExpect(jsonPath("$[0].userShoppingListItemDto[0].status").value("DONE"))
                .andExpect(jsonPath("$[0].customShoppingListItemDto[0].id").value(10))
                .andExpect(jsonPath("$[0].customShoppingListItemDto[0].text").value("Custom shopping item"))
                .andExpect(jsonPath("$[0].customShoppingListItemDto[0].status").value("DONE"));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).getListOfUserAndCustomShoppingListsWithStatusInprogress(userVO.getId(), locale.getLanguage());
    }

    @Test
    void getAllHabitAssignsByHabitIdAndAcquired() throws Exception {
        Long habitId = 1L;
        Locale locale = new Locale("en");
        HabitAssignDto habitAssign = HabitAssignDto.builder()
                .id(100L)
                .userId(5L)
                .duration(21)
                .workingDays(5)
                .habitStreak(3)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .build();

        List<HabitAssignDto> response = List.of(habitAssign);

        when(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitId}/all", habitId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].duration").value(21))
                .andExpect(jsonPath("$[0].workingDays").value(5))
                .andExpect(jsonPath("$[0].habitStreak").value(3))
                .andExpect(jsonPath("$[0].status").value("INPROGRESS"))
                .andExpect(jsonPath("$[0].progressNotificationHasDisplayed").value(false));

        verify(habitAssignService).getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, locale.getLanguage());
    }

    @Test
    void getHabitAssignByHabitId() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;
        Locale locale = new Locale("en");
        HabitAssignDto response = HabitAssignDto.builder()
                .id(100L)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(3)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitId}/active", habitId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(3))
                .andExpect(jsonPath("$.status").value("INPROGRESS"))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage());
    }

    @Test
    void getUsersHabitByHabitAssignId() throws Exception {
        UserVO userVO = getUserVO();
        Long habitAssignId = 1L;
        Locale locale = new Locale("en");
        HabitTranslationDto translation = HabitTranslationDto.builder()
                .name("Test Habit Name")
                .habitItem("Habit Item")
                .description("Habit description")
                .languageCode("en")
                .build();

        ShoppingListItemDto shoppingItem = ShoppingListItemDto.builder()
                .id(1L)
                .text("Shopping item")
                .status("DONE")
                .build();

        CustomShoppingListItemResponseDto customItem = CustomShoppingListItemResponseDto.builder()
                .id(10L)
                .text("Custom item")
                .status(ShoppingListItemStatus.DONE)
                .build();

        HabitDto response = HabitDto.builder()
                .id(100L)
                .defaultDuration(21)
                .amountAcquiredUsers(50L)
                .habitTranslation(translation)
                .image("image.png")
                .complexity(2)
                .tags(List.of("tag1", "tag2"))
                .shoppingListItems(List.of(shoppingItem))
                .customShoppingListItems(List.of(customItem))
                .isCustomHabit(false)
                .usersIdWhoCreatedCustomHabit(null)
                .habitAssignStatus(HabitAssignStatus.INPROGRESS)
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/{habitAssignId}/more", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.defaultDuration").value(21))
                .andExpect(jsonPath("$.amountAcquiredUsers").value(50))
                .andExpect(jsonPath("$.image").value("image.png"))
                .andExpect(jsonPath("$.complexity").value(2))
                .andExpect(jsonPath("$.tags[0]").value("tag1"))
                .andExpect(jsonPath("$.tags[1]").value("tag2"))
                .andExpect(jsonPath("$.isCustomHabit").value(false))
                .andExpect(jsonPath("$.habitAssignStatus").value("INPROGRESS"))
                .andExpect(jsonPath("$.usersIdWhoCreatedCustomHabit").doesNotExist())
                .andExpect(jsonPath("$.habitTranslation.name").value("Test Habit Name"))
                .andExpect(jsonPath("$.habitTranslation.habitItem").value("Habit Item"))
                .andExpect(jsonPath("$.habitTranslation.description").value("Habit description"))
                .andExpect(jsonPath("$.habitTranslation.languageCode").value("en"))
                .andExpect(jsonPath("$.shoppingListItems[0].id").value(1))
                .andExpect(jsonPath("$.shoppingListItems[0].text").value("Shopping item"))
                .andExpect(jsonPath("$.shoppingListItems[0].status").value("DONE"))
                .andExpect(jsonPath("$.customShoppingListItems[0].id").value(10))
                .andExpect(jsonPath("$.customShoppingListItems[0].text").value("Custom item"))
                .andExpect(jsonPath("$.customShoppingListItems[0].status").value("DONE"));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage());
    }

    @Test
    void updateAssignByHabitId() throws Exception {
        Long habitAssignId = 1L;

        HabitAssignStatDto requestDto = new HabitAssignStatDto();
        HabitAssignManagementDto response = HabitAssignManagementDto.builder()
                .id(100L)
                .status(HabitAssignStatus.INPROGRESS)
                .createDateTime(ZonedDateTime.now())
                .habitId(1L)
                .userId(5L)
                .duration(21)
                .workingDays(5)
                .habitStreak(0)
                .lastEnrollment(ZonedDateTime.now())
                .progressNotificationHasDisplayed(false)
                .build();

        requestDto.setStatus(HabitAssignStatus.INPROGRESS);

        when(habitAssignService.updateStatusByHabitAssignId(habitAssignId, requestDto)).thenReturn(response);

        mockMvc.perform(patch(HabitAssignControllerLink + "/{habitAssignId}", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("INPROGRESS"))
                .andExpect(jsonPath("$.habitId").value(1))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(0))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false))
                .andExpect(jsonPath("$.createDateTime").exists())
                .andExpect(jsonPath("$.lastEnrollment").exists());

        verify(habitAssignService).updateStatusByHabitAssignId(habitAssignId, requestDto);
    }

    @Test
    void enrollHabit() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");
        LocalDate date = LocalDate.of(2026, 1, 10);

        HabitAssignDto response = HabitAssignDto.builder()
                .id(100L)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(2)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .createDateTime(ZonedDateTime.now())
                .lastEnrollmentDate(ZonedDateTime.now())
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitAssignId}/enroll/{date}", habitAssignId, date)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(2))
                .andExpect(jsonPath("$.status").value("INPROGRESS"))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false))
                .andExpect(jsonPath("$.createDateTime").exists())
                .andExpect(jsonPath("$.lastEnrollmentDate").exists());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage());
    }

    @Test
    void unenrollHabit() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        LocalDate date = LocalDate.of(2026, 1, 10);

        HabitAssignDto response = HabitAssignDto.builder()
                .id(200L)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(2)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .createDateTime(ZonedDateTime.now())
                .lastEnrollmentDate(ZonedDateTime.now())
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.unenrollHabit(habitAssignId, userVO.getId(), date)).thenReturn(response);

        mockMvc.perform(post(HabitAssignControllerLink + "/{habitAssignId}/unenroll/{date}", habitAssignId, date)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(2))
                .andExpect(jsonPath("$.status").value("INPROGRESS"))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false))
                .andExpect(jsonPath("$.createDateTime").exists())
                .andExpect(jsonPath("$.lastEnrollmentDate").exists());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).unenrollHabit(habitAssignId, userVO.getId(), date);
    }

    @Test
    void getInprogressHabitAssignOnDate() throws Exception {
        UserVO userVO = getUserVO();
        LocalDate date = LocalDate.of(2026, 1, 10);
        Locale locale = new Locale("en");

        HabitAssignDto responseDto = HabitAssignDto.builder()
                .id(300L)
                .userId(userVO.getId())
                .duration(21)
                .workingDays(5)
                .habitStreak(2)
                .status(HabitAssignStatus.INPROGRESS)
                .progressNotificationHasDisplayed(false)
                .createDateTime(ZonedDateTime.now())
                .lastEnrollmentDate(ZonedDateTime.now())
                .build();

        List<HabitAssignDto> response = List.of(responseDto);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/active/{date}", date)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300))
                .andExpect(jsonPath("$[0].userId").value(userVO.getId()))
                .andExpect(jsonPath("$[0].duration").value(21))
                .andExpect(jsonPath("$[0].workingDays").value(5))
                .andExpect(jsonPath("$[0].habitStreak").value(2))
                .andExpect(jsonPath("$[0].status").value("INPROGRESS"))
                .andExpect(jsonPath("$[0].progressNotificationHasDisplayed").value(false))
                .andExpect(jsonPath("$[0].createDateTime").exists())
                .andExpect(jsonPath("$[0].lastEnrollmentDate").exists());

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage());
    }

    @Test
    void getHabitAssignBetweenDates() throws Exception {
        UserVO userVO = getUserVO();
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 6, 10);
        Locale locale = new Locale("en");

        HabitEnrollDto habitEnrollDto = HabitEnrollDto.builder()
                .habitAssignId(101L)
                .habitName("Test Habit")
                .habitDescription("Test Description")
                .isEnrolled(true)
                .build();

        HabitsDateEnrollmentDto enrollmentDto = HabitsDateEnrollmentDto.builder()
                .enrollDate(LocalDate.of(2026, 1, 15))
                .habitAssigns(List.of(habitEnrollDto))
                .build();

        List<HabitsDateEnrollmentDto> response = List.of(enrollmentDto);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage())).thenReturn(response);

        mockMvc.perform(get(HabitAssignControllerLink + "/activity/{from}/to/{to}", from, to)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, locale.getLanguage())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].habitAssigns.length()").value(1))
                .andExpect(jsonPath("$[0].habitAssigns[0].habitAssignId").value(101))
                .andExpect(jsonPath("$[0].habitAssigns[0].habitName").value("Test Habit"))
                .andExpect(jsonPath("$[0].habitAssigns[0].habitDescription").value("Test Description"));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitAssignService).findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage());
    }

    @Test
    void cancelHabitAssign() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();

        HabitAssignDto response = HabitAssignDto.builder()
                .id(10L)
                .userId(userVO.getId())
                .status(HabitAssignStatus.CANCELLED)
                .duration(21)
                .workingDays(5)
                .habitStreak(2)
                .progressNotificationHasDisplayed(false)
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitAssignService.cancelHabitAssign(habitId, userVO.getId())).thenReturn(response);

        mockMvc.perform(patch(HabitAssignControllerLink + "/cancel/{habitId}", habitId)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(userVO.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.duration").value(21))
                .andExpect(jsonPath("$.workingDays").value(5))
                .andExpect(jsonPath("$.habitStreak").value(2))
                .andExpect(jsonPath("$.progressNotificationHasDisplayed").value(false));

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
                .content(objectMapper.writeValueAsString(requestDto)))
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
