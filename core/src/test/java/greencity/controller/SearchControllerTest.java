package greencity.controller;

import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.service.LanguageService;
import greencity.service.SearchService;
import greencity.service.UserService;
import greencity.validator.LanguageValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private SearchController searchController;

    @Mock
    private SearchService searchService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private LanguageService languageService;

    @BeforeEach
    void setUp() {
        when(languageService.findAllLanguageCodes()).thenReturn(List.of("en", "ua"));

        Validator validator = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new ConstraintValidatorFactory() {
                    @Override
                    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                        if (key == LanguageValidator.class) {
                            LanguageValidator validator = new LanguageValidator();
                            ReflectionTestUtils.setField(validator, "languageService", languageService);
                            return (T) validator;
                        }
                        try {
                            return key.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void releaseInstance(ConstraintValidator<?, ?> instance) {}
                })
                .buildValidatorFactory()
                .getValidator();

        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper),
                        new PageableHandlerMethodArgumentResolver() // Нужно для обработки page и size
                )
                .setValidator(new SpringValidatorAdapter(validator))
                .build();
    }

    @Test
    void searchEverythingTest() throws Exception {
        String query = "eco";
        SearchResponseDto responseDto = SearchResponseDto.builder()
                .ecoNews(Collections.emptyList())
                .countOfResults(0L)
                .build();

        when(searchService.search(query, "en")).thenReturn(responseDto);

        mockMvc.perform(get("/search")
                        .param("searchQuery", query)
                        .locale(Locale.ENGLISH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countOfResults").value(0));

        verify(searchService).search(query, "en");
    }

    @Test
    void searchEcoNewsTest() throws Exception {
        String query = "eco";
        Pageable pageable = PageRequest.of(0, 5);
        PageableDto<SearchNewsDto> pageableDto = new PageableDto<>(Collections.emptyList(), 0L, 0, 1);

        when(searchService.searchAllNews(pageable, query, "en")).thenReturn(pageableDto);

        mockMvc.perform(get("/search/econews")
                        .param("searchQuery", query)
                        .param("page", "0")
                        .param("size", "5")
                        .locale(Locale.ENGLISH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(searchService).searchAllNews(pageable, query, "en");
    }
}