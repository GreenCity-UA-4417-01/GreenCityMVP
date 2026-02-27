package greencity.mapping;


import greencity.dto.category.CategoryDtoResponse;
import greencity.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class CategoryDtoResponseMapperTest {
    @InjectMocks
    CategoryDtoResponseMapper mapper;

    @Test
    void convertTest(){
        Category category = Category.builder()
                .id(1l)
                .name("Bob")
                .build();

        CategoryDtoResponse result = mapper.convert(category);

        assertEquals(1l,result.getId());
        assertEquals("Bob",result.getName());

    }
}
