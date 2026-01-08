package greencity.controller;

import greencity.GreenCityApplication;
import greencity.service.LanguageService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@WebMvcTest(LanguageController.class)
@ContextConfiguration(classes = GreenCityApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class LanguageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LanguageService languageService;
    @MockBean
    private UserService userService;
    @MockBean
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        when(languageService.findAllLanguageCodes()).thenReturn(List.of("en", "ua", "fr"));
    }

    @Test
    void getAllLanguageCodesTest() throws Exception {
        mockMvc.perform(get("/language"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"en\", \"ua\", \"fr\"]"));
        verify(languageService).findAllLanguageCodes();
    }
}
