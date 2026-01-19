package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.dto.habit.AddCustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {
    private static final String HABIT_PATH = "/habit";
    private static final String LANG = "en";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @Mock
    private HabitService habitService;

    @Mock
    private TagsService tagsService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private HabitController habitController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(habitController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void getHabitById_ShouldReturnHabitAndOkStatus_WhenHabitExists() throws Exception {
        Long habitId = 1L;
        HabitDto habitDto = HabitDto.builder()
                .id(habitId)
                .complexity(1)
                .build();

        when(habitService.getByIdAndLanguageCode(habitId, LANG)).thenReturn(habitDto);

        mockMvc.perform(get(HABIT_PATH + "/{id}", habitId)
                .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(habitId));

        verify(habitService).getByIdAndLanguageCode(habitId, LANG);
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void getHabitById_ShouldReturnNotFoundStatus_WhenHabitNotFound() throws Exception {
        Long habitId = 999L;

        when(habitService.getByIdAndLanguageCode(habitId, LANG)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(HABIT_PATH + "/{id}", habitId)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isNotFound());

        verify(habitService).getByIdAndLanguageCode(habitId, LANG);
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void getHabitById_ShouldReturnBadRequest_WhenIdIsNotLong() throws Exception {
        mockMvc.perform(get(HABIT_PATH + "/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService, tagsService, userService, modelMapper);
    }

    @Test
    void getAll_ShouldReturnPageableDtoAndOkStatus_WhenValidRequest() throws Exception {
        String email = "user@test.com";
        UserVO userVO = new UserVO();
        userVO.setId(42L);

        when(userService.findByEmail(email)).thenReturn(userVO);

        HabitDto habit1 = HabitDto.builder().id(1L).complexity(1).build();
        HabitDto habit2 = HabitDto.builder().id(2L).complexity(2).build();

        PageableDto<HabitDto> response =
                new PageableDto<>(List.of(habit1, habit2), 2L, 0, 1);

        when(habitService.getAllHabitsByLanguageCode(
                eq(userVO),
                any(Pageable.class),
                eq(LANG)))
                .thenReturn(response);

        mockMvc.perform(get(HABIT_PATH)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .principal((Principal) () -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").isArray())
                .andExpect(jsonPath("$.page", hasSize(2)))
                .andExpect(jsonPath("$.page[0].id").value(1))
                .andExpect(jsonPath("$.page[1].id").value(2));

        verify(userService).findByEmail(email);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(habitService).getAllHabitsByLanguageCode(
                eq(userVO), pageableCaptor.capture(),
                eq(LANG));
        verifyNoMoreInteractions(habitService);

        Pageable captured = pageableCaptor.getValue();
        assertNotNull(captured);
        assertEquals(0, captured.getPageNumber());
    }

    @Test
    void getAll_ShouldPassPaginationParamsToService_WhenPageableParamsProvided() throws Exception {
        String email = "user@test.com";
        UserVO userVO = new UserVO();
        userVO.setId(42L);

        when(userService.findByEmail(email)).thenReturn(userVO);

        PageableDto<HabitDto> response = new PageableDto<>(List.of(), 0L, 2, 1);

        when(habitService.getAllHabitsByLanguageCode(
                eq(userVO),
                any(Pageable.class),
                eq(LANG)))
                .thenReturn(response);

        mockMvc.perform(get(HABIT_PATH)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .principal((Principal) () -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").isArray())
                .andExpect(jsonPath("$.page", hasSize(0)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(habitService).getAllHabitsByLanguageCode(
                eq(userVO),
                pageableCaptor.capture(),
                eq(LANG));

        Pageable captured = pageableCaptor.getValue();
        assertEquals(2, captured.getPageNumber());
        assertEquals(10, captured.getPageSize());

        Sort.Order idOrder = captured.getSort().getOrderFor("id");
        assertNotNull(idOrder);
        assertEquals(Sort.Direction.DESC, idOrder.getDirection());

        verify(userService).findByEmail(email);
        verifyNoMoreInteractions(habitService);
    }


    @Test
    void getAll_ShouldReturnNotFound_WhenServiceThrowsNotFoundException() throws Exception {

        String email = "user@test.com";
        UserVO userVO = new UserVO();
        userVO.setId(42L);

        when(userService.findByEmail(email)).thenReturn(userVO);
        when(habitService.getAllHabitsByLanguageCode(
                eq(userVO),
                any(Pageable.class),
                eq(LANG)))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get(HABIT_PATH)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .principal((Principal) () -> email))
                .andExpect(status().isNotFound());

        verify(userService).findByEmail(email);
        verify(habitService).getAllHabitsByLanguageCode(
                eq(userVO),
                any(Pageable.class),
                eq(LANG));
        verifyNoMoreInteractions(habitService);
    }


    @Test
    void getShoppingListItems_ShouldReturnShoppingListAndOkStatus_WhenHabitExists() throws Exception {
        Long habitId = 1L;

        when(habitService.getShoppingListForHabit(habitId, LANG)).thenReturn(List.of());

        mockMvc.perform(get(HABIT_PATH + "/{id}/shopping-list", habitId)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(habitService).getShoppingListForHabit(habitId, LANG);
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void getShoppingListItems_ShouldReturnNotFound_WhenServiceThrowsNotFoundException() throws Exception {
        Long habitId = 1L;

        when(habitService.getShoppingListForHabit(habitId, LANG)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(HABIT_PATH + "/{id}/shopping-list", habitId)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isNotFound());

        verify(habitService).getShoppingListForHabit(habitId, LANG);
        verifyNoMoreInteractions(habitService);
    }


    @Test
    void getAllByTagsAndLanguageCode_ShouldReturnPageableDtoAndOkStatus_WhenValidRequest() throws Exception {
        HabitDto habit1 = HabitDto.builder().id(1L).complexity(1).build();
        HabitDto habit2 = HabitDto.builder().id(2L).complexity(2).build();
        PageableDto<HabitDto> response = new PageableDto<>(List.of(
                habit1,
                habit2),
                2L,
                0,
                1);

        List<String> tags = List.of("eco", "recycling");

        when(habitService.getAllByTagsAndLanguageCode(
                any(Pageable.class),
                eq(tags),
                eq(LANG)))
                .thenReturn(response);

        mockMvc.perform(get(HABIT_PATH + "/tags/search")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .param("tags", "eco", "recycling")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page", hasSize(2)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(habitService).getAllByTagsAndLanguageCode(
                pageableCaptor.capture(),
                eq(tags),
                eq(LANG));

        verifyNoMoreInteractions(habitService);

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
    }

    @Test
    void getAllByTagsAndLanguageCode_ShouldPassPaginationParamsToService_WhenProvided() throws Exception {
        List<String> tags = List.of("eco");
        PageableDto<HabitDto> response = new PageableDto<>(List.of(), 0L, 2, 1);

        when(habitService.getAllByTagsAndLanguageCode(
                any(Pageable.class),
                eq(tags),
                eq(LANG)))
                .thenReturn(response);

        mockMvc.perform(get(HABIT_PATH + "/tags/search")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .param("tags", "eco")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page").isArray())
                .andExpect(jsonPath("$.page", hasSize(0)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(habitService).getAllByTagsAndLanguageCode(
                pageableCaptor.capture(),
                eq(tags),
                eq(LANG));
        verifyNoMoreInteractions(habitService);

        Pageable captured = pageableCaptor.getValue();
        assertAll("Verify Pageable mapping",
                () -> assertEquals(2, captured.getPageNumber()),
                () -> assertEquals(10, captured.getPageSize()),
                () -> {
                    Sort.Order idOrder = captured.getSort().getOrderFor("id");
                    assertNotNull(idOrder);
                    assertEquals(Sort.Direction.DESC, idOrder.getDirection());
                }
        );
    }


    @Test
    void getAllByTagsAndLanguageCode_ShouldReturnBadRequest_WhenTagsParamMissing() throws Exception {
        mockMvc.perform(get(HABIT_PATH + "/tags/search")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }


    @Test
    void getAllByDifferentParameters_ShouldPassPaginationAndSortParamsToService_WhenProvided() throws Exception {
        String email = "user@test.com";
        UserVO userVO = new UserVO();
        userVO.setId(42L);
        when(userService.findByEmail(email)).thenReturn(userVO);

        List<String> tags = List.of("eco");
        PageableDto<HabitDto> response = new PageableDto<>(List.of(), 0L, 2, 1);

        when(habitService.getAllByDifferentParameters(
                eq(userVO),
                any(Pageable.class),
                eq(Optional.of(tags)),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(LANG)))
                .thenReturn(response);

        mockMvc.perform(get(HABIT_PATH + "/search")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .param("tags", "eco")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .principal((Principal) () -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.page", hasSize(0)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(userService).findByEmail(email);
        verify(habitService).getAllByDifferentParameters(
                eq(userVO),
                pageableCaptor.capture(),
                eq(Optional.of(tags)),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(LANG));
        verifyNoMoreInteractions(habitService);

        Pageable captured = pageableCaptor.getValue();
        assertAll("Verify Pageable mapping",
                () -> assertEquals(2, captured.getPageNumber()),
                () -> assertEquals(10, captured.getPageSize()),
                () -> {
                    Sort.Order idOrder = captured.getSort().getOrderFor("id");
                    assertNotNull(idOrder);
                    assertEquals(Sort.Direction.DESC, idOrder.getDirection());
                }
        );
    }

    @Test
    void getAllByDifferentParameters_ShouldReturnBadRequest_WhenNoFilterParamsProvided() throws Exception {
        String email = "user@test.com";
        UserVO userVO = new UserVO();
        userVO.setId(42L);
        when(userService.findByEmail(email)).thenReturn(userVO);

        mockMvc.perform(get(HABIT_PATH + "/search")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG)
                        .principal((Principal) () -> email))
                .andExpect(status().isBadRequest());

        verify(userService).findByEmail(email);
        verifyNoInteractions(habitService);
    }


    @Test
    void findAllHabitsTags_ShouldReturnTagsAndOkStatus_WhenValidLangProvided() throws Exception {
        List<String> tags = List.of(
                "Resource Saving",
                "Smart Consuming",
                "Reusable"
        );

        when(tagsService.findAllHabitsTags(LANG)).thenReturn(tags);

        mockMvc.perform(get(HABIT_PATH + "/tags")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]").value("Resource Saving"))
                .andExpect(jsonPath("$[1]").value("Smart Consuming"))
                .andExpect(jsonPath("$[2]").value("Reusable"));

        verify(tagsService).findAllHabitsTags(LANG);
        verifyNoMoreInteractions(tagsService);
        verifyNoInteractions(habitService);
    }

    @Test
    void findAllHabitsTags_ShouldReturnOkStatusAndEmptyArray_WhenNoTagsExist() throws Exception {
        when(tagsService.findAllHabitsTags(LANG)).thenReturn(List.of());

        mockMvc.perform(get(HABIT_PATH + "/tags")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, LANG))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(tagsService).findAllHabitsTags(LANG);
        verifyNoMoreInteractions(tagsService);
    }

    @Test
    void addCustomHabit_ShouldReturnCreatedAndResponseBody_WhenValidRequestWithImage() throws Exception {
        String email = "user@test.com";

        AddCustomHabitDtoRequest request = AddCustomHabitDtoRequest.builder()
                .complexity(2)
                .defaultDuration(14)
                .tagIds(Set.of(1L, 2L))
                .build();

        AddCustomHabitDtoResponse response = AddCustomHabitDtoResponse.builder()
                .id(10L)
                .userId(42L)
                .complexity(2)
                .defaultDuration(14)
                .tagIds(Set.of(1L, 2L))
                .image("some-image-url")
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "habit.png",
                MediaType.IMAGE_PNG_VALUE,
                "png-bytes".getBytes(StandardCharsets.UTF_8)
        );

        when(habitService.addCustomHabit(any(AddCustomHabitDtoRequest.class), any(MultipartFile.class), eq(email)))
                .thenReturn(response);

        mockMvc.perform(multipart(HABIT_PATH + "/custom")
                        .file(requestPart)
                        .file(imagePart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.complexity").value(2))
                .andExpect(jsonPath("$.defaultDuration").value(14))
                .andExpect(jsonPath("$.image").value("some-image-url"))
                .andExpect(jsonPath("$.tagIds").isArray())
                .andExpect(jsonPath("$.tagIds", containsInAnyOrder(1, 2)));

        ArgumentCaptor<AddCustomHabitDtoRequest> requestCaptor =
                ArgumentCaptor.forClass(AddCustomHabitDtoRequest.class);
        ArgumentCaptor<MultipartFile> imageCaptor =
                ArgumentCaptor.forClass(MultipartFile.class);

        verify(habitService).addCustomHabit(
                requestCaptor.capture(),
                imageCaptor.capture(),
                eq(email));
        verifyNoMoreInteractions(habitService);

        AddCustomHabitDtoRequest capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(2, capturedRequest.getComplexity());
        assertEquals(14, capturedRequest.getDefaultDuration());
        assertEquals(Set.of(1L, 2L), capturedRequest.getTagIds());

        MultipartFile capturedImage = imageCaptor.getValue();
        assertNotNull(capturedImage);
        assertEquals("habit.png", capturedImage.getOriginalFilename());
    }

    @Test
    void addCustomHabit_ShouldReturnCreated_WhenValidRequestWithoutImage() throws Exception {
        String email = "user@test.com";

        AddCustomHabitDtoRequest request = AddCustomHabitDtoRequest.builder()
                .complexity(1)
                .defaultDuration(7)
                .tagIds(Set.of(1L))
                .build();

        AddCustomHabitDtoResponse response = AddCustomHabitDtoResponse.builder()
                .id(11L)
                .userId(42L)
                .complexity(1)
                .defaultDuration(7)
                .tagIds(Set.of(1L))
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        when(habitService.addCustomHabit(any(AddCustomHabitDtoRequest.class), isNull(), eq(email)))
                .thenReturn(response);

        mockMvc.perform(multipart(HABIT_PATH + "/custom")
                        .file(requestPart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.complexity").value(1))
                .andExpect(jsonPath("$.defaultDuration").value(7))
                .andExpect(jsonPath("$.tagIds").isArray())
                .andExpect(jsonPath("$.tagIds", containsInAnyOrder(1)));

        verify(habitService).addCustomHabit(
                any(AddCustomHabitDtoRequest.class),
                isNull(),
                eq(email));
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void addCustomHabit_ShouldReturnBadRequest_WhenRequestPartMissing() throws Exception {
        String email = "user@test.com";

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "habit.png",
                MediaType.IMAGE_PNG_VALUE,
                "png-bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HABIT_PATH + "/custom")
                        .file(imagePart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void addCustomHabit_ShouldReturnBadRequest_WhenComplexityIsNull() throws Exception {
        String email = "user@test.com";

        AddCustomHabitDtoRequest invalidRequest = AddCustomHabitDtoRequest.builder()
                .complexity(null) // violates @NotNull
                .defaultDuration(7)
                .tagIds(Set.of(1L))
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(invalidRequest)
        );

        mockMvc.perform(multipart(HABIT_PATH + "/custom")
                        .file(requestPart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void addCustomHabit_ShouldReturnNotFound_WhenServiceThrowsNotFoundException() throws Exception {
        String email = "user@test.com";

        AddCustomHabitDtoRequest request = AddCustomHabitDtoRequest.builder()
                .complexity(2)
                .defaultDuration(14)
                .tagIds(Set.of(1L))
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "habit.png",
                MediaType.IMAGE_PNG_VALUE,
                "x".getBytes(StandardCharsets.UTF_8)
        );

        when(habitService.addCustomHabit(
                any(AddCustomHabitDtoRequest.class),
                any(MultipartFile.class),
                eq(email)))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(multipart(HABIT_PATH + "/custom")
                        .file(requestPart)
                        .file(imagePart)
                        .principal(() -> email)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());

        verify(habitService).addCustomHabit(
                any(AddCustomHabitDtoRequest.class),
                any(MultipartFile.class),
                eq(email));
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void getFriendsAssignedToHabitProfilePictures_ShouldReturnListAndOkStatus_WhenValidRequest() throws Exception {
        String email = "user@test.com";
        long habitId = 1L;

        UserVO userVO = new UserVO();
        userVO.setId(42L);

        when(userService.findByEmail(email)).thenReturn(userVO);

        UserProfilePictureDto dto1 = UserProfilePictureDto.builder()
                .id(100L)
                .name("Alice")
                .profilePicturePath("path-1")
                .build();

        UserProfilePictureDto dto2 = UserProfilePictureDto.builder()
                .id(200L)
                .name("Bob")
                .profilePicturePath("path-2")
                .build();

        when(habitService.getFriendsAssignedToHabitProfilePictures(habitId, 42L))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get(HABIT_PATH + "/{habitId}/friends/profile-pictures", habitId)
                        .principal(() -> email))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].profilePicturePath").value("path-1"))
                .andExpect(jsonPath("$[1].id").value(200))
                .andExpect(jsonPath("$[1].name").value("Bob"))
                .andExpect(jsonPath("$[1].profilePicturePath").value("path-2"));

        verify(userService).findByEmail(email);

        ArgumentCaptor<Long> habitIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(habitService).getFriendsAssignedToHabitProfilePictures(
                habitIdCaptor.capture(),
                userIdCaptor.capture());
        verifyNoMoreInteractions(habitService);

        assertEquals(habitId, habitIdCaptor.getValue());
        assertEquals(42L, userIdCaptor.getValue());
    }

    @Test
    void getFriendsAssignedToHabitProfilePictures_ShouldReturnBadRequest_WhenHabitIdIsNotLong() throws Exception {
        mockMvc.perform(get(HABIT_PATH + "/{habitId}/friends/profile-pictures", "invalid-id")
                        .principal(() -> "user@test.com"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService, tagsService, userService, modelMapper);
    }

    @Test
    void getFriendsAssignedToHabitProfilePictures_ShouldReturnNotFound_WhenServiceThrowsNotFoundException() throws Exception {
        String email = "user@test.com";
        long habitId = 999L;

        UserVO userVO = new UserVO();
        userVO.setId(42L);

        when(userService.findByEmail(email)).thenReturn(userVO);

        when(habitService.getFriendsAssignedToHabitProfilePictures(habitId, 42L))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get(HABIT_PATH + "/{habitId}/friends/profile-pictures", habitId)
                        .principal(() -> email))
                .andExpect(status().isNotFound());

        verify(userService).findByEmail(email);
        verify(habitService).getFriendsAssignedToHabitProfilePictures(habitId, 42L);
        verifyNoMoreInteractions(habitService);
    }
}
