package greencity.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.GetHabitStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitRate;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitStatisticService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
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

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
                .setCustomArgumentResolvers(new UserArgumentResolver(userService, modelMapper))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void findAllByHabitId_ShouldReturnStatsAndOkStatus_WhenHabitExists() throws Exception {
        // given
        Long habitId = 1L;
        Long amountOfUsersAcquired = 111L;
        ZonedDateTime now = ZonedDateTime.now();
        HabitStatisticDto statDto1 = HabitStatisticDto.builder()
                .id(11L)
                .habitRate(HabitRate.DEFAULT)
                .createDate(now)
                .amountOfItems(2)
                .habitAssignId(21L)
                .build();
        HabitStatisticDto statDto2 = HabitStatisticDto.builder()
                .id(12L)
                .habitRate(HabitRate.NORMAL)
                .createDate(now.minusDays(1))
                .amountOfItems(3)
                .habitAssignId(22L)
                .build();
        GetHabitStatisticDto getStatDto = GetHabitStatisticDto.builder()
                .amountOfUsersAcquired(amountOfUsersAcquired)
                .habitStatisticDtoList(Arrays.asList(statDto1, statDto2))
                .build();

        when(habitStatisticService.findAllStatsByHabitId(eq(habitId))).thenReturn(getStatDto);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/{habitId}", habitId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amountOfUsersAcquired").value(amountOfUsersAcquired))
                .andExpect(jsonPath("$.habitStatisticDtoList").isArray())
                .andExpect(jsonPath("$.habitStatisticDtoList", hasSize(2)))
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
                        .value(statDto2.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .andExpect(status().isOk());

        verify(habitStatisticService, times(1)).findAllStatsByHabitId(eq(habitId));
    }

    @Test
    void findAllByHabitId_ShouldReturnNotFoundStatus_WhenHabitDoesNotExist() throws Exception {
        // given
        Long fakeId = 999L;
        when(habitStatisticService.findAllStatsByHabitId(eq(fakeId))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/{habitId}", fakeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).findAllStatsByHabitId(eq(fakeId));
    }

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnStatsListAndOkStatus_WhenHabitAssignExists() throws Exception {
        // given
        Long habitAssignId = 1L;
        ZonedDateTime now = ZonedDateTime.now();
        HabitStatisticDto statDto1 = HabitStatisticDto.builder()
                .id(11L)
                .habitRate(HabitRate.DEFAULT)
                .createDate(now)
                .amountOfItems(2)
                .habitAssignId(habitAssignId)
                .build();
        HabitStatisticDto statDto2 = HabitStatisticDto.builder()
                .id(12L)
                .habitRate(HabitRate.NORMAL)
                .createDate(now.minusDays(1))
                .amountOfItems(3)
                .habitAssignId(habitAssignId)
                .build();

        when(habitStatisticService.findAllStatsByHabitAssignId(eq(habitAssignId)))
                .thenReturn(Arrays.asList(statDto1, statDto2));

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
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
                        .value(statDto2.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .andExpect(status().isOk());

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(habitAssignId));
    }

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnEmptyStatsListAndOkStatus_WhenNoStatsExists() throws Exception {
        // given
        Long habitAssignId = 1L;
        when(habitStatisticService.findAllStatsByHabitAssignId(eq(habitAssignId))).thenReturn(List.of());

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(status().isOk());

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(habitAssignId));
    }

    @Test
    void findAllStatsByHabitAssignId_ShouldReturnNotFoundStatus_WhenHabitAssignDoesNotExist() throws Exception {
        // given
        Long fakeId = 999L;
        when(habitStatisticService.findAllStatsByHabitAssignId(eq(fakeId))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(get(HABIT_STATISTIC_PATH + "/assign/{habitAssignId}", fakeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(habitStatisticService, times(1)).findAllStatsByHabitAssignId(eq(fakeId));
    }

    @Test
    void saveHabitStatistic_ShouldReturnStatsAndCreatedStatus_WhenValidInput() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        AddHabitStatisticDto addStatDto = addHabitStatisticDto();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long habitId = 1L;
        HabitStatisticDto statDto = HabitStatisticDto.builder()
                .id(11L)
                .habitRate(addStatDto.getHabitRate())
                .createDate(addStatDto.getCreateDate())
                .amountOfItems(addStatDto.getAmountOfItems())
                .habitAssignId(21L)
                .build();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(habitStatisticService.saveByHabitIdAndUserId(eq(habitId),
                eq(userVO.getId()),
                assertArg(dto -> {
                    assertThat(dto.getHabitRate()).isEqualTo(addStatDto.getHabitRate());
                    assertThat(dto.getAmountOfItems()).isEqualTo(addStatDto.getAmountOfItems());
                    assertThat(dto.getCreateDate().toInstant()).isEqualTo(addStatDto.getCreateDate().toInstant());
                })))
                .thenReturn(statDto);

        // When & Then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{habitId}", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(addStatDto))
                        .principal(principal))
                .andExpect(jsonPath("$.id").value(statDto.getId()))
                .andExpect(jsonPath("$.habitRate").value(addStatDto.getHabitRate().name()))
                .andExpect(jsonPath("$.amountOfItems").value(addStatDto.getAmountOfItems()))
                .andExpect(jsonPath("$.habitAssignId").value(statDto.getHabitAssignId()))
                .andExpect(jsonPath("$.createDate")
                        .value(addStatDto.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).findByEmail(principal.getName());
        verify(habitStatisticService, times(1))
                .saveByHabitIdAndUserId(eq(habitId),
                        eq(userVO.getId()),
                        assertArg(dto -> {
                            assertThat(dto.getHabitRate()).isEqualTo(addStatDto.getHabitRate());
                            assertThat(dto.getAmountOfItems()).isEqualTo(addStatDto.getAmountOfItems());
                            assertThat(dto.getCreateDate().toInstant())
                                    .isEqualTo(addStatDto.getCreateDate().toInstant());
                        })
                );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAddInputData")
    void saveHabitStatistic_ShouldReturnBadRequestStatus_WhenInvalidInput(String invalidInput) throws Exception {
        // When & Then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{habitId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidInput)
                        .principal(getPrincipal()))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never())
                .saveByHabitIdAndUserId(anyLong(), anyLong(), any(AddHabitStatisticDto.class));
    }

    @Test
    void saveHabitStatistic_ShouldReturnNotFoundStatus_WhenHabitDoesNotExist() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        AddHabitStatisticDto addStatDto = addHabitStatisticDto();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long fakeId = 999L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(habitStatisticService.saveByHabitIdAndUserId(eq(fakeId),
                eq(userVO.getId()),
                assertArg(dto -> {
                    assertThat(dto.getHabitRate()).isEqualTo(addStatDto.getHabitRate());
                    assertThat(dto.getAmountOfItems()).isEqualTo(addStatDto.getAmountOfItems());
                    assertThat(dto.getCreateDate().toInstant()).isEqualTo(addStatDto.getCreateDate().toInstant());
                })))
                .thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(post(HABIT_STATISTIC_PATH + "/{id}", fakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(addStatDto))
                        .principal(principal))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByEmail(principal.getName());
        verify(habitStatisticService, times(1))
                .saveByHabitIdAndUserId(eq(fakeId),
                        eq(userVO.getId()),
                        assertArg(dto -> {
                            assertThat(dto.getHabitRate()).isEqualTo(addStatDto.getHabitRate());
                            assertThat(dto.getAmountOfItems()).isEqualTo(addStatDto.getAmountOfItems());
                            assertThat(dto.getCreateDate().toInstant())
                                    .isEqualTo(addStatDto.getCreateDate().toInstant());
                        })
                );
    }

    @Test
    void updateStatistic_ShouldReturnUpdatedStatsAndOkStatus_WhenValidInput() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();

        UpdateHabitStatisticDto updateDto = UpdateHabitStatisticDto.builder()
                .amountOfItems(3)
                .habitRate(HabitRate.DEFAULT)
                .build();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long statisticId = 1L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(habitStatisticService.update(eq(statisticId), eq(userVO.getId()), eq(updateDto))).thenReturn(updateDto);

        // When & Then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", statisticId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto))
                        .principal(principal))
                .andExpect(jsonPath("$.amountOfItems").value(updateDto.getAmountOfItems()))
                .andExpect(jsonPath("$.habitRate").value(updateDto.getHabitRate().name()))
                .andExpect(status().isOk());

        verify(userService, times(1)).findByEmail(principal.getName());
        verify(habitStatisticService, times(1))
                .update(eq(statisticId), eq(userVO.getId()), eq(updateDto));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUpdateInputData")
    void updateStatistic_ShouldReturnBadRequestStatus_WhenInvalidInput(String invalidInput) throws Exception {
        // When & Then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidInput)
                        .principal(getPrincipal()))
                .andExpect(status().isBadRequest());

        verify(habitStatisticService, never())
                        .update(anyLong(), anyLong(), any(UpdateHabitStatisticDto.class));
    }

    @Test
    void updateStatistic_ShouldReturnNotFoundStatus_WhenHabitStatsDoesNotExist() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();

        UpdateHabitStatisticDto updateDto = UpdateHabitStatisticDto.builder()
                .amountOfItems(3)
                .habitRate(HabitRate.DEFAULT)
                .build();
        Principal principal = getPrincipal();
        UserVO userVO = getUserVO();
        Long fakeId = 999L;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);
        when(habitStatisticService.update(eq(fakeId), eq(userVO.getId()), eq(updateDto))).thenThrow(NotFoundException.class);

        // when & then
        mockMvc.perform(put(HABIT_STATISTIC_PATH + "/{id}", fakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto))
                        .principal(principal))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByEmail(principal.getName());
        verify(habitStatisticService, times(1)).update(eq(fakeId), eq(userVO.getId()), eq(updateDto));
    }

    private static Stream<String> provideInvalidAddInputData() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return Stream.of("",
                "{}",
                mapper.writeValueAsString(new AddHabitStatisticDto()),
                mapper.writeValueAsString(new AddHabitStatisticDto(null, HabitRate.NORMAL, ZonedDateTime.now())),
                mapper.writeValueAsString(new AddHabitStatisticDto(-1, HabitRate.NORMAL, ZonedDateTime.now())),
                mapper.writeValueAsString(new AddHabitStatisticDto(20, HabitRate.NORMAL, ZonedDateTime.now())),
                mapper.writeValueAsString(new AddHabitStatisticDto(0, null, ZonedDateTime.now())),
                mapper.writeValueAsString(new AddHabitStatisticDto(5, null, ZonedDateTime.now())),
                mapper.writeValueAsString(new AddHabitStatisticDto(5, HabitRate.NORMAL, null))
        );
    }

    private static Stream<String> provideInvalidUpdateInputData() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of("",
                "{}",
                mapper.writeValueAsString(new UpdateHabitStatisticDto()),
                mapper.writeValueAsString(new UpdateHabitStatisticDto(null, HabitRate.DEFAULT)),
                mapper.writeValueAsString(new UpdateHabitStatisticDto(-1, HabitRate.DEFAULT)),
                mapper.writeValueAsString(new UpdateHabitStatisticDto(20, HabitRate.DEFAULT)),
                mapper.writeValueAsString(new UpdateHabitStatisticDto(0, null)),
                mapper.writeValueAsString(new UpdateHabitStatisticDto(5, null))
        );
    }
}