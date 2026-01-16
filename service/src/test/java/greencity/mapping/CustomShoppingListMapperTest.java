package greencity.mapping;


import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.entity.CustomShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class CustomShoppingListMapperTest {
    @InjectMocks
    CustomShoppingListMapper mapper;

    @Test
    void convertTest(){
        CustomShoppingListItemResponseDto item = CustomShoppingListItemResponseDto.builder()
                .id(1l)
                .text("aaa")
                .status(ShoppingListItemStatus.ACTIVE)
                .build();

        CustomShoppingListItem result = mapper.convert(item);

        assertEquals(1l,result.getId());
        assertEquals("aaa",result.getText());
        assertEquals(ShoppingListItemStatus.ACTIVE,result.getStatus());
    }

    @Test
    void allToListTest(){
        CustomShoppingListItemResponseDto item1 = CustomShoppingListItemResponseDto.builder()
                .id(1l)
                .text("item1")
                .status(ShoppingListItemStatus.ACTIVE)
                .build();
        CustomShoppingListItemResponseDto item2 = CustomShoppingListItemResponseDto.builder()
                .id(2l)
                .text("item2")
                .status(ShoppingListItemStatus.DONE)
                .build();
        List<CustomShoppingListItemResponseDto> dtoList = List.of(item1,item2);
        List<CustomShoppingListItem> itemList = mapper.mapAllToList(dtoList);

        assertEquals(2,itemList.size());

        assertAll(
                ()->assertEquals(1l,itemList.get(0).getId()),
                ()->assertEquals("item1",itemList.get(0).getText()),
                ()->assertEquals(ShoppingListItemStatus.ACTIVE,itemList.get(0).getStatus()),
                ()->assertEquals(2l,itemList.get(1).getId()),
                ()->assertEquals("item2",itemList.get(1).getText()),
                ()->assertEquals(ShoppingListItemStatus.DONE,itemList.get(1).getStatus())
        );

    }


}
