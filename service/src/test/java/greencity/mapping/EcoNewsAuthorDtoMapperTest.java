package greencity.mapping;


import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class EcoNewsAuthorDtoMapperTest {
    @InjectMocks
    EcoNewsAuthorDtoMapper mapper;

    @Test
    void convertTest(){
        User user = User.builder()
                .id(1l)
                .name("Bob")
                .build();
        EcoNewsAuthorDto result = mapper.convert(user);

        assertEquals(1l,result.getId());
        assertEquals("Bob",result.getName());


    }



}
