package greencity.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habitstatistic.*;
import greencity.dto.user.UserVO;
import greencity.enums.HabitRate;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitStatisticService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static greencity.ModelUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitStatisticControllerTest {

    private static final String HABIT_STATISTIC_PATH = "/habit/statistic";

    private MockMvc mockMvc;

    @Mock
    private HabitStatisticService habitStatisticService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private HabitStatisticController habitStatisticController;

    private static final ObjectMapper objectMapper =  new ObjectMapper();

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeAll
    static void init() {
        objectMapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
                .setCustomArgumentResolvers(new UserArgumentResolver(userService, modelMapper))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    // ========== GET /habit/statistic/{habitId} Tests ==========

    @Test
    void findAllByHabitId_ShouldReturnStatsAndOkStatus_WhenHabitExists() throws Exception {
        // given
        Long habitId = 1L;
        Long amountOfUsersAcquired = 111L;
        ZonedDateTime now = ZonedDateTime.now();
        HabitStatisticDto statDto1 = createHabitStatisticDto(11L, HabitRate.DEFAULT, now, 2, 21L);
        HabitStatisticDto statDto2 = createHabitStatisticDto(12L, HabitRate.NORMAL, now.minusDays(1), 3, 22L);
        GetHabitStatisticDto getStatDto = GetHabitStatisticDto.builder()
                .amountOfUsersAcquired(amountOfUsersAcquired)
                .habitStatisticDtoList(Arrays.asList(statDto1, statDto2))
                .build();

        when(habitStatisticService.findAllStatsByHabitId(eq(habitId))).thenReturn(getStatDto);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/{habitId}", habitId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountOfUsersAcquired").value(amountOfUsersAcquired))
                .andExpect(isArrayWithSize(2, ".habitStatisticDtoList"))
                .andExpect(jsonPath("$.habitStatisticDtoList[0].id").value(statDto1.getId()))
                .andExpect(jsonPath("$.habitStatisticDtoList[1].id").value(statDto2.getId()))
                .andExpect(jsonPath("$.habitStatisticDtoList[0].habitRate")
                        .value(statDto1.getHabitRate().name()))
                .andExpect(jsonPath("$.habitStatisticDtoList[1].habitRate")
                        .value(statDto2.getHabitRate().name()))
                .andExpect(jsonPath("$.habitStatisticDtoList[0].amountOfItems")
                        .value(statDto1.getAmountOfItems()))
                .andExpect(jsonPath("$.habitStatisticDtoList[1].amountOfItems")
                        .value(statDto2.getAmountOfItems()))
                .andExpect(jsonPath("$.habitStatisticDtoList[0].habitAssignId")
                        .value(statDto1.getHabitAssignId()))
                .andExpect(jsonPath("$.habitStatisticDtoList[1].habitAssignId")
                        .value(statDto2.getHabitAssignId()))
                .andExpect(jsonPath("$.habitStatisticDtoList[0].createDate")
                        .value(statDto1.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .andExpect(jsonPath("$.habitStatisticDtoList[1].createDate")
                        .value(statDto2.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        verify(habitStatisticService, times(1)).findAllStatsByHabitId(eq(habitId));
    }

    @Test
    void findAllByHabitId_ShouldReturnNotFoundStatus_WhenHabitDoesNotExist() throws Exception {
        // given
        Long nonExistentId = 999L;
        when(habitStatisticService.findAllStatsByHabitId(eq(nonExistentId))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/{habitId}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).findAllStatsByHabitId(eq(nonExistentId));
    }

    // ========== GET /habit/statistic/assign/{habitAssignId} Tests ==========

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnStatsListAndOkStatus_WhenHabitAssignExists() throws Exception {
        // given
        Long habitAssignId = 1L;
        ZonedDateTime now = ZonedDateTime.now();
        HabitStatisticDto statDto1 = createHabitStatisticDto(11L, HabitRate.DEFAULT, now, 2, habitAssignId);
        HabitStatisticDto statDto2 = createHabitStatisticDto(12L, HabitRate.NORMAL, now.minusDays(1), 3, habitAssignId);

        when(habitStatisticService.findAllStatsByHabitAssignId(eq(habitAssignId)))
                .thenReturn(Arrays.asList(statDto1, statDto2));

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", habitAssignId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(isArrayWithSize(2, ""))
                .andExpect(jsonPath("$[0].id").value(statDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(statDto2.getId()))
                .andExpect(jsonPath("$[0].habitRate").value(statDto1.getHabitRate().name()))
                .andExpect(jsonPath("$[1].habitRate").value(statDto2.getHabitRate().name()))
                .andExpect(jsonPath("$[0].amountOfItems").value(statDto1.getAmountOfItems()))
                .andExpect(jsonPath("$[1].amountOfItems").value(statDto2.getAmountOfItems()))
                .andExpect(jsonPath("$[0].habitAssignId").value(statDto1.getHabitAssignId()))
                .andExpect(jsonPath("$[1].habitAssignId").value(statDto2.getHabitAssignId()))
                .andExpect(jsonPath("$[0].createDate")
                        .value(statDto1.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .andExpect(jsonPath("$[1].createDate")
                        .value(statDto2.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(habitAssignId));
    }

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnEmptyStatsListAndOkStatus_WhenNoStatsExists() throws Exception {
        // given
        Long habitAssignId = 1L;
        when(habitStatisticService.findAllStatsByHabitAssignId(eq(habitAssignId))).thenReturn(List.of());

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", habitAssignId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(habitAssignId));
    }

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnNotFoundStatus_WhenHabitAssignDoesNotExist() throws Exception {
        // given
        Long nonExistentId = 999L;
        when(habitStatisticService.findAllStatsByHabitAssignId(eq(nonExistentId))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(nonExistentId));
    }

    // ========== POST /habit/statistic/{habitId} Tests ==========

    @Test
    void saveHabitStatistic_ShouldReturnStatsAndCreatedStatus_WhenValidInput() throws Exception {
        // Given
        AddHabitStatisticDto addStatDto = addHabitStatisticDto();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long habitId = 1L;
        HabitStatisticDto statDto = createHabitStatisticDto(11L, addStatDto.getHabitRate(),
                addStatDto.getCreateDate(), addStatDto.getAmountOfItems(), 21L);

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);
        when(habitStatisticService.saveByHabitIdAndUserId(eq(habitId), eq(userVO.getId()),
                assertArg(dto -> assertAddHabitStatisticDto(dto, addStatDto))))
                .thenReturn(statDto);

        // When & Then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{habitId}", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addStatDto))
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(statDto.getId()))
                .andExpect(jsonPath("$.habitRate").value(addStatDto.getHabitRate().name()))
                .andExpect(jsonPath("$.amountOfItems").value(addStatDto.getAmountOfItems()))
                .andExpect(jsonPath("$.habitAssignId").value(statDto.getHabitAssignId()))
                .andExpect(jsonPath("$.createDate")
                        .value(addStatDto.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        verify(userService, times(1)).findByEmail(eq(principal.getName()));
        verify(habitStatisticService, times(1))
                .saveByHabitIdAndUserId(eq(habitId), eq(userVO.getId()),
                        assertArg(dto -> assertAddHabitStatisticDto(dto, addStatDto))
                );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAddInputData")
    void saveHabitStatistic_ShouldReturnBadRequestStatus_WhenInvalidInput(String invalidInput) throws Exception {
        // When & Then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{habitId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidInput)
                        .principal(getPrincipal()))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never()).saveByHabitIdAndUserId(anyLong(), anyLong(), any(AddHabitStatisticDto.class));
    }

    @Test
    void saveHabitStatistic_ShouldReturnNotFoundStatus_WhenHabitDoesNotExist() throws Exception {
        // given
        AddHabitStatisticDto addStatDto = addHabitStatisticDto();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long nonExistentId = 999L;

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);
        when(habitStatisticService.saveByHabitIdAndUserId(eq(nonExistentId), eq(userVO.getId()),
                assertArg(dto -> assertAddHabitStatisticDto(dto, addStatDto))))
                .thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addStatDto))
                        .principal(principal))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByEmail(eq(principal.getName()));
        verify(habitStatisticService, times(1))
                .saveByHabitIdAndUserId(eq(nonExistentId), eq(userVO.getId()),
                        assertArg(dto -> assertAddHabitStatisticDto(dto, addStatDto)));
    }

    // ========== PUT /habit/statistic/{id} Tests ==========

    @Test
    void updateStatistic_ShouldReturnUpdatedStatsAndOkStatus_WhenValidInput() throws Exception {
        // Given
        UpdateHabitStatisticDto updateDto = UpdateHabitStatisticDto.builder()
                .amountOfItems(3)
                .habitRate(HabitRate.DEFAULT)
                .build();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long statisticId = 1L;

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);
        when(habitStatisticService.update(eq(statisticId), eq(userVO.getId()), eq(updateDto))).thenReturn(updateDto);

        // When & Then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", statisticId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountOfItems").value(updateDto.getAmountOfItems()))
                .andExpect(jsonPath("$.habitRate").value(updateDto.getHabitRate().name()));

        verify(userService, times(1)).findByEmail(eq(principal.getName()));
        verify(habitStatisticService, times(1)).update(eq(statisticId), eq(userVO.getId()), eq(updateDto));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUpdateInputData")
    void updateStatistic_ShouldReturnBadRequestStatus_WhenInvalidInput(String invalidInput) throws Exception {
        // When & Then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidInput)
                        .principal(getPrincipal()))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never()).update(anyLong(), anyLong(), any(UpdateHabitStatisticDto.class));
    }

    @Test
    void updateStatistic_ShouldReturnNotFoundStatus_WhenHabitStatsDoesNotExist() throws Exception {
        // given
        UpdateHabitStatisticDto updateDto = UpdateHabitStatisticDto.builder()
                .amountOfItems(3)
                .habitRate(HabitRate.DEFAULT)
                .build();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long nonExistentId = 999L;

        when(userService.findByEmail(eq(principal.getName()))).thenReturn(userVO);
        when(habitStatisticService.update(eq(nonExistentId), eq(userVO.getId()), eq(updateDto))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .principal(principal))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByEmail(eq(principal.getName()));
        verify(habitStatisticService, times(1)).update(eq(nonExistentId), eq(userVO.getId()), eq(updateDto));
    }

    // ========== GET /habit/statistic/todayStatisticsForAllHabitItems Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {"uk", "en"})
    void getTodayStatisticsForAllHabitItems_ShouldReturnStatisticsAndOkStatus(String language) throws Exception {
        // Given
        HabitItemsAmountStatisticDto dto1 = new HabitItemsAmountStatisticDto("Item_1", 3);
        HabitItemsAmountStatisticDto dto2 = new HabitItemsAmountStatisticDto("Item_2", 5);

        when(habitStatisticService.getTodayStatisticsForAllHabitItems(eq(language))).thenReturn(List.of(dto1, dto2));

        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/todayStatisticsForAllHabitItems")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(isArrayWithSize(2, ""))
                .andExpect(jsonPath("$[0].habitItem").value(dto1.getHabitItem()))
                .andExpect(jsonPath("$[1].habitItem").value(dto2.getHabitItem()))
                .andExpect(jsonPath("$[0].notTakenItems").value(dto1.getNotTakenItems()))
                .andExpect(jsonPath("$[1].notTakenItems").value(dto2.getNotTakenItems()));

        verify(habitStatisticService, times(1)).getTodayStatisticsForAllHabitItems(eq(language));
    }

    // ========== GET /habit/statistic/acquired/count Tests ==========

    @ParameterizedTest
    @ValueSource(longs = {0L, 2L, 10L})
    void findAmountOfAcquiredHabits_ShouldReturnCountAndOkStatus_WhenUserExists(Long expectedCount) throws Exception {
        // Given
        Long userId = 1L;
        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(eq(userId))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/acquired/count")
                        .param("userId", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(expectedCount));

        verify(habitStatisticService, times(1)).getAmountOfAcquiredHabitsByUserId(eq(userId));
    }

    @Test
    void findAmountOfAcquiredHabits_ShouldReturnNotFoundStatus_WhenUserDoesNotExist() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(eq(nonExistentId))).thenThrow(NotFoundException.class);

        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/acquired/count")
                        .param("userId", nonExistentId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).getAmountOfAcquiredHabitsByUserId(eq(nonExistentId));
    }

    @Test
    void findAmountOfAcquiredHabits_ShouldHandleMissingUserId() throws Exception {
        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/acquired/count")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never()).getAmountOfAcquiredHabitsByUserId(anyLong());
    }

    // ========== GET /habit/statistic/in-progress/count Tests ==========

    @ParameterizedTest
    @ValueSource(longs = {0L, 2L, 10L})
    void findAmountOfHabitsInProgress_ShouldReturnCountAndOkStatus_WhenUserExists(Long expectedCount) throws Exception {
        // Given
        Long userId = 1L;
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(eq(userId))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/in-progress/count")
                        .param("userId", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(expectedCount));

        verify(habitStatisticService, times(1)).getAmountOfHabitsInProgressByUserId(eq(userId));
    }

    @Test
    void findAmountOfHabitsInProgress_ShouldReturnNotFoundStatus_WhenUserDoesNotExist() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(eq(nonExistentId))).thenThrow(NotFoundException.class);

        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/in-progress/count")
                        .param("userId", nonExistentId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).getAmountOfHabitsInProgressByUserId(eq(nonExistentId));
    }

    @Test
    void findAmountOfHabitsInProgress_ShouldHandleMissingUserId() throws Exception {
        // When & Then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/in-progress/count")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never()).getAmountOfHabitsInProgressByUserId(anyLong());
    }

    // ========== Helper Methods ==========

    private HabitStatisticDto createHabitStatisticDto(Long id, HabitRate habitRate, ZonedDateTime createDate,
                                                      Integer amountOfItems, Long habitAssignId) {
        return HabitStatisticDto.builder()
                .id(id)
                .habitRate(habitRate)
                .createDate(createDate)
                .amountOfItems(amountOfItems)
                .habitAssignId(habitAssignId)
                .build();
    }

    private ResultMatcher isArrayWithSize(int size, String jsonPropertyPath) {
        return result -> {
            jsonPath("$" + jsonPropertyPath).isArray().match(result);
            jsonPath("$" + jsonPropertyPath, hasSize(size)).match(result);
        };
    }

    private void assertAddHabitStatisticDto(AddHabitStatisticDto actual, AddHabitStatisticDto expected) {
        assertThat(actual.getHabitRate()).isEqualTo(expected.getHabitRate());
        assertThat(actual.getAmountOfItems()).isEqualTo(expected.getAmountOfItems());
        assertThat(actual.getCreateDate().toInstant()).isEqualTo(expected.getCreateDate().toInstant());
    }

    // ========== Test Data Providers ==========

    private static Stream<String> provideInvalidAddInputData() throws JsonProcessingException {
        return Stream.of("",
                "{}",
                objectMapper.writeValueAsString(new AddHabitStatisticDto()),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(null, HabitRate.NORMAL, ZonedDateTime.now())),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(-1, HabitRate.NORMAL, ZonedDateTime.now())),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(20, HabitRate.NORMAL, ZonedDateTime.now())),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(0, null, ZonedDateTime.now())),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(5, null, ZonedDateTime.now())),
                objectMapper.writeValueAsString(new AddHabitStatisticDto(5, HabitRate.NORMAL, null))
        );
    }

    private static Stream<String> provideInvalidUpdateInputData() throws JsonProcessingException {
        return Stream.of("",
                "{}",
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto()),
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto(null, HabitRate.DEFAULT)),
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto(-1, HabitRate.DEFAULT)),
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto(20, HabitRate.DEFAULT)),
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto(0, null)),
                objectMapper.writeValueAsString(new UpdateHabitStatisticDto(5, null))
        );
    }
}