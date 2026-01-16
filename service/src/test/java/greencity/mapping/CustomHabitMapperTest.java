package greencity.mapping;


import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.entity.Habit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class CustomHabitMapperTest {
    @InjectMocks
    CustomHabitMapper mapper;

    @Test
    void convertTest(){
        AddCustomHabitDtoRequest addCustomHabitDtoRequest = AddCustomHabitDtoRequest.builder()
                .complexity(2)
                .defaultDuration(30)
                .defaultDuration(30)
                .image("image.png")
                .build();
        Habit habit = mapper.convert(addCustomHabitDtoRequest);
        assertEquals("image.png",habit.getImage());
        assertEquals(2,habit.getComplexity());
        assertEquals(30,habit.getDefaultDuration());
        Assertions.assertTrue(habit.getIsCustomHabit());
    }
}
