package greencity.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.PageableDto;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitFactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HabitFactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private HabitFactService habitFactService;
    @InjectMocks
    private HabitFactController habitFactController;
    private static final String FACTS_URL = "/facts";
    @Mock
    private Validator mockValidator;

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(habitFactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(mockValidator)
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes,objectMapper))
                .build();
    }


    @Test
    void getHabitFactOfTheDay() throws Exception {
        Long languageId = 1L;
        LanguageTranslationDTO dto = new LanguageTranslationDTO();
        when(habitFactService.getHabitFactOfTheDay(languageId))
                .thenReturn(dto);
        mockMvc.perform(get(FACTS_URL + "/dayFact/{languageId}", languageId))
                .andExpect(status().isOk());

        verify(habitFactService).getHabitFactOfTheDay(languageId);
    }

    @Test
    void getRandomFactByHabitId() throws Exception {
        Long habitId = 1l;
        String language = "en";
        LanguageTranslationDTO dto = new LanguageTranslationDTO();

        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, language))
                .thenReturn(dto);

        mockMvc.perform(
                        get(FACTS_URL + "/random/{habitId}", habitId)
                                .locale(Locale.ENGLISH)
                )
                .andExpect(status().isOk());
        verify(habitFactService).getRandomHabitFactByHabitIdAndLanguage(habitId, language);
    }

    @Test
    void getAll() throws Exception {
        PageableDto<LanguageTranslationDTO> pageableDto = new PageableDto<>(
                List.of(new LanguageTranslationDTO()),
                1,
                0,
                1
        );
        when(habitFactService.getAllHabitFacts(any(), eq("en")))
                .thenReturn(pageableDto);
        mockMvc.perform(get(FACTS_URL))
                .andExpect(status().isOk());
        verify(habitFactService).getAllHabitFacts(any(), eq("en"));
    }

    @Test
    void save() throws Exception {
        HabitFactVO habitFactVO = new HabitFactVO();
        HabitFactDtoResponse responseDto = new HabitFactDtoResponse();

        when(habitFactService.save(any(HabitFactPostDto.class)))
                .thenReturn(habitFactVO);
        when(modelMapper.map(habitFactVO, HabitFactDtoResponse.class))
                .thenReturn(responseDto);

        mockMvc.perform(post("/facts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());

        verify(habitFactService).save(any(HabitFactPostDto.class));
        verify(modelMapper).map(habitFactVO, HabitFactDtoResponse.class);
    }

    @Test
    void update() throws Exception {
        Long id = 1l;
        HabitFactPostDto postDto = new HabitFactPostDto();
        HabitFactVO updated = new HabitFactVO();
        when(habitFactService.update(any(HabitFactUpdateDto.class), eq(id)))
                .thenReturn(updated);
        when(modelMapper.map(updated, HabitFactPostDto.class))
                .thenReturn(postDto);
        mockMvc.perform(
                put(FACTS_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isOk());
        verify(habitFactService)
                .update(any(HabitFactUpdateDto.class), eq(id));
    }

    @Test
    void deleteById() throws Exception {
        Long id = 1l;
        mockMvc.perform(delete(FACTS_URL + "/{id}", id))
                .andExpect(status().isOk());
        verify(habitFactService).delete(id);
    }
    @Test
    void deleteInvalid() throws Exception {
        Long invalidId = -1l;
        when(habitFactService.delete(invalidId)).thenThrow(BadRequestException.class);
        mockMvc.perform(delete(FACTS_URL+"/{id}",invalidId))
                .andExpect(status().isBadRequest());
        verify(habitFactService).delete(invalidId);
    }


}
