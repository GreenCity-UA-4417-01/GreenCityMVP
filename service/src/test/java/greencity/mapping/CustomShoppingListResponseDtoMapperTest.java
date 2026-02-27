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
public class CustomShoppingListResponseDtoMapperTest {
    @InjectMocks
    CustomShoppingListResponseDtoMapper mapper;

    @Test
    void convertTest(){
        CustomShoppingListItem item = CustomShoppingListItem.builder()
                .id(1l)
                .text("aaa")
                .status(ShoppingListItemStatus.ACTIVE)
                .build();
        CustomShoppingListItemResponseDto result = mapper.convert(item);

        assertEquals(1l,result.getId());
        assertEquals("aaa",result.getText());
        assertEquals(ShoppingListItemStatus.ACTIVE,result.getStatus());
    }
    @Test
    void allToListTest(){
        CustomShoppingListItem item1 = CustomShoppingListItem.builder()
                .id(1l)
                .text("item1")
                .status(ShoppingListItemStatus.ACTIVE)
                .build();
        CustomShoppingListItem item2 = CustomShoppingListItem.builder()
                .id(2l)
                .text("item2")
                .status(ShoppingListItemStatus.DONE)
                .build();
        List<CustomShoppingListItem> itemList = List.of(item1,item2);
        List<CustomShoppingListItemResponseDto> dtoList = mapper.mapAllToList(itemList);

        assertEquals(2,dtoList.size());
        assertAll(
                ()->assertEquals(1l,dtoList.get(0).getId()),
                ()->assertEquals("item1",dtoList.get(0).getText()),
                ()->assertEquals(ShoppingListItemStatus.ACTIVE,dtoList.get(0).getStatus()),

                ()->assertEquals(2l,dtoList.get(1).getId()),
                ()->assertEquals("item2",dtoList.get(1).getText()),
                ()->assertEquals(ShoppingListItemStatus.DONE,dtoList.get(1).getStatus())



        );
    }



}
