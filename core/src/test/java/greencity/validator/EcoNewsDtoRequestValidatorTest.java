package greencity.validator;

import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstants;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.InvalidURLException;
import greencity.exception.exceptions.WrongCountOfTagsException;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EcoNewsDtoRequestValidatorTest {

    private static final int MAX_TAGS_QTY = ValidationConstants.MAX_AMOUNT_OF_TAGS;
    private static final String ERR_MSG = ErrorMessage.WRONG_COUNT_OF_TAGS_EXCEPTION;

    @InjectMocks
    private EcoNewsDtoRequestValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    private AddEcoNewsDtoRequest request;

    @BeforeEach
    void setUp() {
        this.request = new AddEcoNewsDtoRequest();
    }

    @ParameterizedTest
    @MethodSource("provideValidTestData")
    void isValid_ShouldReturnTrue_WhenValidTags(String source, int numberOfTags, int urlValidatorCalls) {
        // given
        this.request.setSource(source);
        this.request.setTags(createTagsList(numberOfTags));

        // when & then
        try (MockedStatic<UrlValidator> urlValidator = mockStatic(UrlValidator.class)) {
            urlValidator.when(() -> UrlValidator.isUrlValid(eq(this.request.getSource()))).thenReturn(true);

            assertTrue(validator.isValid(this.request, context));
            urlValidator.verify(() -> UrlValidator.isUrlValid(eq(this.request.getSource())), times(urlValidatorCalls));
        }
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTestData")
    void isValid_ShouldThrowWrongCountOfTagsException_WhenInvalidTags(String source, int numberOfTags, int urlValidatorCalls) {
        // given
        this.request.setSource(source);
        this.request.setTags(createTagsList(numberOfTags));

        // when & then
        try (MockedStatic<UrlValidator> urlValidator = mockStatic(UrlValidator.class)) {
            urlValidator.when(() -> UrlValidator.isUrlValid(eq(this.request.getSource()))).thenReturn(true);

            assertThatThrownBy(() -> validator.isValid(this.request, context))
                    .isInstanceOf(WrongCountOfTagsException.class)
                    .hasMessage(ERR_MSG);
            urlValidator.verify(() -> UrlValidator.isUrlValid(this.request.getSource()), times(urlValidatorCalls));
        }
    }

    @ParameterizedTest
    @MethodSource("provideUrlValidatorExceptions")
    void isValid_ShouldThrowException_WhenUrlValidatorThrowsException(Throwable exception) {
        // given
        this.request.setSource("https://example.com");
        this.request.setTags(createTagsList(1));

        // when & then
        try (MockedStatic<UrlValidator> urlValidator = mockStatic(UrlValidator.class)) {
            urlValidator.when(() -> UrlValidator.isUrlValid(eq(this.request.getSource()))).thenThrow(exception);

            assertThatThrownBy(() -> validator.isValid(this.request, context)).isSameAs(exception);
            urlValidator.verify(() -> UrlValidator.isUrlValid(this.request.getSource()), times(1));
        }
    }

    // ========== Test Data Providers ==========

    private static Stream<Arguments> provideValidTestData() {
        return Stream.of(
                Arguments.of("https://example.com", 1, 1),
                Arguments.of("https://example.com", 2, 1),
                Arguments.of("https://example.com", MAX_TAGS_QTY, 1),
                Arguments.of("   ", 1, 1),
                Arguments.of("   ", 2, 1),
                Arguments.of("   ", MAX_TAGS_QTY, 1),
                // With empty source
                Arguments.of("", 1, 0),
                Arguments.of("", 2, 0),
                Arguments.of("", MAX_TAGS_QTY, 0),
                // With null source
                Arguments.of(null, 1, 0),
                Arguments.of(null, 2, 0),
                Arguments.of(null, MAX_TAGS_QTY, 0)
        );
    }

    private static Stream<Arguments> provideInvalidTestData() {
        return Stream.of(
                Arguments.of("https://example.com", 0, 1),
                Arguments.of("https://example.com", MAX_TAGS_QTY + 1, 1),
                // With empty source
                Arguments.of("", 0, 0),
                Arguments.of("", MAX_TAGS_QTY + 1, 0),
                // With null source
                Arguments.of(null, 0, 0),
                Arguments.of(null, MAX_TAGS_QTY + 1, 0)
        );
    }

    static Stream<Throwable> provideUrlValidatorExceptions() {
        return Stream.of(new InvalidURLException("InvalidURLException"), new RuntimeException("RuntimeException"));
    }

    // ========== Helper Methods ==========

    private List<String> createTagsList(int numberOfTags) {
        return IntStream.rangeClosed(1, numberOfTags)
                .mapToObj(i -> "tag" + i)
                .collect(Collectors.toList());
    }
}