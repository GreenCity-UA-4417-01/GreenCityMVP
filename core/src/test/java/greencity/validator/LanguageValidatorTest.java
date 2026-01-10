package greencity.validator;

import greencity.annotations.ValidLanguage;
import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageValidatorTest {
    @InjectMocks
    private LanguageValidator languageValidator;
    @Mock
    private LanguageService languageService;
    @Mock
    private ValidLanguage validLanguage;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        when(languageService.findAllLanguageCodes()).thenReturn(List.of("en", "ua"));
        languageValidator.initialize(validLanguage);
    }

    //check language code of ENG in the list and has return true
    @Test
    void isValidTrueTest() {
        boolean result = languageValidator.isValid(Locale.ENGLISH, constraintValidatorContext);
        assertTrue(result);
    }

    //check language code of FRENCH in the list and has return false
    @Test
    void isValidFalseTest() {
        boolean result = languageValidator.isValid(Locale.FRENCH, constraintValidatorContext);
        assertFalse(result);
    }
}
