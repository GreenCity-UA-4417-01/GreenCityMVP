package greencity.mapping;


import greencity.dto.category.CategoryDto;
import greencity.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class CategoryDtoMapperTest {
    @InjectMocks
    private CategoryDtoMapper categoryDtoMapper;


    @Test
    void convertTest(){
        CategoryDto categoryDto = CategoryDto.builder()
                .name("Eco")
                .nameUa("Еко")
                .parentCategoryId(1l)
                .build();
        Category result = categoryDtoMapper.convert(categoryDto);
        assertEquals("Eco",result.getName());
    }

}
