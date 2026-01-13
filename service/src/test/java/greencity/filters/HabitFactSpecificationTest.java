package greencity.filters;

import greencity.dto.habitfact.HabitFactViewDto;
import greencity.entity.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class HabitFactSpecificationTest {
    @Mock
    private CriteriaBuilder criteriaBuilderMock;

    @Mock
    private CriteriaQuery criteriaQueryMock;

    @Mock
    private Root<HabitFact> habitFactRootMock;

    @Mock
    private Root<HabitFactTranslation> habitFactTranslationRootMock;

    @Mock
    private Predicate predicateMock;

    @Mock
    private Predicate andIdPredicate;

    @Mock
    private Predicate andHabitIdPredicate;

    @Mock
    private Predicate andContentPredicate;

    @Mock
    private Predicate likePredicateMock;

    @Mock
    private Predicate finalPredicateMock;

    @Mock
    private Predicate equalPredicateMock;

    @Mock
    private SingularAttribute<HabitFact, Habit> habit;

    @Mock
    private SingularAttribute<Habit, Long> id;

    @Mock
    private SingularAttribute<Translation, String> content;

    @Mock
    private SingularAttribute<HabitFactTranslation, HabitFact> habitFact;

    @Mock
    private Path<Long> pathHabitFactIdMock;

    @Mock
    private Path<Long> pathHabitIdMock;

    @Mock
    private Path<HabitFact> pathHabitFactMock;

    @Mock
    private Path<String> contentPathMock;

    @Mock
    private ListJoin<HabitFact, Habit> habitJoinMock;

    private List<SearchCriteria> criteriaList;

    private HabitFactSpecification habitFactSpecification;

    @BeforeEach
    public void setUp() {
        HabitFact_.habit = habit;
        Habit_.id = id;
        HabitFactTranslation_.habitFact = habitFact;
        HabitFactTranslation_.content = content;
    }

    @Test
    void toPredicateOfMultipleSearchCriteria() {

        HabitFactViewDto dto = new HabitFactViewDto("1", "2", "eco");

        criteriaList = new ArrayList<>();

        criteriaList.add(SearchCriteria.builder()
                .key("id")
                .type("id")
                .value(dto.getId())
                .build());

        criteriaList.add(SearchCriteria.builder()
                .key("habitId")
                .type("habitId")
                .value(dto.getHabitId())
                .build());

        criteriaList.add(SearchCriteria.builder()
                .key("content")
                .type("content")
                .value(dto.getContent())
                .build());

        habitFactSpecification = new HabitFactSpecification(criteriaList);

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);


        when(habitFactRootMock.get(HabitFact_.id)).thenReturn(pathHabitFactIdMock);

        when(criteriaBuilderMock.equal(
                habitFactRootMock.get(HabitFact_.ID),
                criteriaList.get(0).getValue()
        )).thenReturn(andIdPredicate);

        when(criteriaBuilderMock.and(predicateMock, andIdPredicate)).thenReturn(andIdPredicate);


        when(habitFactRootMock.join(HabitFact_.habit)).thenReturn(habitJoinMock);

        when(habitJoinMock.get(Habit_.id)).thenReturn(pathHabitIdMock);

        when(criteriaBuilderMock.equal(pathHabitIdMock, criteriaList.get(1).getValue())).thenReturn(andHabitIdPredicate);

        when(criteriaBuilderMock.and(andIdPredicate, andHabitIdPredicate)).thenReturn(andHabitIdPredicate);


        when(criteriaQueryMock.from(HabitFactTranslation.class)).thenReturn(habitFactTranslationRootMock);

        when(pathHabitFactMock.get(HabitFact_.id)).thenReturn(pathHabitFactIdMock);

        when(habitFactTranslationRootMock.get(HabitFactTranslation_.habitFact)).thenReturn(pathHabitFactMock);

        when(criteriaBuilderMock.equal(pathHabitFactIdMock, pathHabitFactIdMock)).thenReturn(equalPredicateMock);

        when(habitFactTranslationRootMock.get(content)).thenReturn(contentPathMock);

        when(criteriaBuilderMock.like(contentPathMock, "%" + criteriaList.get(2).getValue() + "%")).thenReturn(likePredicateMock);

        when(criteriaBuilderMock.and(likePredicateMock, equalPredicateMock)).thenReturn(andContentPredicate);

        when(criteriaBuilderMock.and(predicateMock, andContentPredicate)).thenReturn(finalPredicateMock);

        habitFactSpecification.toPredicate(habitFactRootMock, criteriaQueryMock, criteriaBuilderMock);

        verify(criteriaBuilderMock).conjunction();

        verify(criteriaBuilderMock).and(predicateMock, andIdPredicate);
        verify(criteriaBuilderMock).and(andIdPredicate, andHabitIdPredicate);
        verify(criteriaBuilderMock).and(andHabitIdPredicate, andContentPredicate);

        verify(habitFactRootMock).get(HabitFact_.id);
        verify(criteriaBuilderMock).equal(habitFactRootMock.get(HabitFact_.ID), criteriaList.get(0).getValue());
        verify(criteriaBuilderMock).and(predicateMock, andIdPredicate);
    }

    @Test
    void toPredicateShouldReturnValidPredicateWhenSearchingById() {
        HabitFactViewDto dto = new HabitFactViewDto("1", "", "");

        criteriaList = new ArrayList<>();

        criteriaList.add(SearchCriteria.builder()
                .key("id")
                .type("id")
                .value(dto.getId())
                .build());

        habitFactSpecification = new HabitFactSpecification(criteriaList);

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);

        when(habitFactRootMock.get(HabitFact_.id)).thenReturn(pathHabitFactIdMock);

        when(criteriaBuilderMock.equal(
                habitFactRootMock.get(HabitFact_.ID),
                criteriaList.get(0).getValue()
        )).thenReturn(andIdPredicate);

        when(criteriaBuilderMock.and(predicateMock, andIdPredicate)).thenReturn(andIdPredicate);

        Predicate result = habitFactSpecification.toPredicate(habitFactRootMock, criteriaQueryMock, criteriaBuilderMock);

        verify(criteriaBuilderMock).conjunction();
        verify(habitFactRootMock, atLeastOnce()).get(HabitFact_.ID);
        verify(criteriaBuilderMock).equal(habitFactRootMock.get(HabitFact_.ID), criteriaList.get(0).getValue());

        verify(criteriaBuilderMock).and(predicateMock, andIdPredicate);

        assertNotNull(result);
        assertEquals(andIdPredicate, result);
    }

    @Test
    void toPredicateShouldHandleFilteringByHabitIdWithAJoin() {
        HabitFactViewDto dto = new HabitFactViewDto("", "2", "");

        criteriaList = new ArrayList<>();

        criteriaList.add(SearchCriteria.builder()
                .key("habitId")
                .type("habitId")
                .value(dto.getHabitId())
                .build());

        habitFactSpecification = new HabitFactSpecification(criteriaList);

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);

        when(habitFactRootMock.join(HabitFact_.habit)).thenReturn(habitJoinMock);

        when(habitJoinMock.get(Habit_.id)).thenReturn(pathHabitIdMock);

        when(criteriaBuilderMock.equal(pathHabitIdMock, criteriaList.get(0).getValue())).thenReturn(andHabitIdPredicate);

        when(criteriaBuilderMock.and(predicateMock, andHabitIdPredicate)).thenReturn(andHabitIdPredicate);

        Predicate result = habitFactSpecification.toPredicate(habitFactRootMock, criteriaQueryMock, criteriaBuilderMock);

        assertEquals(andHabitIdPredicate, result);

        verify(criteriaBuilderMock).conjunction();
        verify(habitFactRootMock).join(HabitFact_.habit);
        verify(habitJoinMock).get(Habit_.id);
        verify(criteriaBuilderMock).equal(pathHabitIdMock, criteriaList.get(0).getValue());
        verify(criteriaBuilderMock).and(predicateMock, andHabitIdPredicate);
    }

    @Test
    void toPredicateShouldFilterByContent() {
        HabitFactViewDto dto = new HabitFactViewDto("", "", "eco");

        criteriaList = new ArrayList<>();

        criteriaList.add(SearchCriteria.builder()
                .key("content")
                .type("content")
                .value(dto.getContent())
                .build());

        habitFactSpecification = new HabitFactSpecification(criteriaList);

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);

        when(criteriaQueryMock.from(HabitFactTranslation.class)).thenReturn(habitFactTranslationRootMock);

        when(pathHabitFactMock.get(HabitFact_.id)).thenReturn(pathHabitFactIdMock);

        when(habitFactTranslationRootMock.get(HabitFactTranslation_.habitFact)).thenReturn(pathHabitFactMock);

        when(criteriaBuilderMock.equal(pathHabitFactIdMock, pathHabitFactIdMock)).thenReturn(equalPredicateMock);

        when(habitFactTranslationRootMock.get(content)).thenReturn(contentPathMock);

        when(criteriaBuilderMock.like(contentPathMock, "%" + criteriaList.get(0).getValue() + "%")).thenReturn(likePredicateMock);

        when(criteriaBuilderMock.and(likePredicateMock, equalPredicateMock)).thenReturn(andContentPredicate);

        when(habitFactRootMock.get(HabitFact_.id)).thenReturn(pathHabitFactIdMock);

        when(criteriaBuilderMock.and(predicateMock, andContentPredicate)).thenReturn(finalPredicateMock);

        Predicate result = habitFactSpecification.toPredicate(habitFactRootMock, criteriaQueryMock, criteriaBuilderMock);

        assertEquals(finalPredicateMock, result);

        verify(criteriaBuilderMock).conjunction();

        verify(criteriaQueryMock).from(HabitFactTranslation.class);

        verify(habitFactTranslationRootMock).get(content);

        verify(criteriaBuilderMock).like(contentPathMock, "%" + dto.getContent() + "%");

        verify(habitFactTranslationRootMock).get(HabitFactTranslation_.habitFact);

        verify(pathHabitFactMock).get(HabitFact_.id);

        verify(criteriaBuilderMock).equal(pathHabitFactIdMock, pathHabitFactIdMock);

        verify(criteriaBuilderMock).and(likePredicateMock, equalPredicateMock);

        verify(criteriaBuilderMock).and(predicateMock, andContentPredicate);
    }

    @Test
    void toPredicateShouldNotAddPredicateWhenContentIsEmpty() {
        HabitFactViewDto dto = new HabitFactViewDto("", "", "");

        criteriaList = new ArrayList<>();
        criteriaList.add(SearchCriteria.builder()
                .key("content")
                .type("content")
                .value(dto.getContent())
                .build());

        habitFactSpecification = new HabitFactSpecification(criteriaList);

        when(criteriaQueryMock.from(HabitFactTranslation.class)).thenReturn(habitFactTranslationRootMock);
        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);
        when(criteriaBuilderMock.and(predicateMock, predicateMock))
                .thenReturn(predicateMock); // ← КРИТИЧНО

        Predicate result =
                habitFactSpecification.toPredicate(
                        habitFactRootMock,
                        criteriaQueryMock,
                        criteriaBuilderMock
                );

        assertNotNull(result);
        assertEquals(predicateMock, result);

        verify(criteriaBuilderMock, times(2)).conjunction();

        verify(criteriaQueryMock).from(HabitFactTranslation.class);
        verify(criteriaBuilderMock, never()).like(any(), anyString());
        verify(criteriaBuilderMock, never()).equal(any(), any());
    }

}