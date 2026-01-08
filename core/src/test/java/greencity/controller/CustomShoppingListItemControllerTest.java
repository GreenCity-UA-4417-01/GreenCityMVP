package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.CustomShoppingListItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class CustomShoppingListItemControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Mock
    private CustomShoppingListItemService customShoppingListItemService;

    @InjectMocks
    private CustomShoppingListItemController controller;

    @BeforeEach
    void setUp() {
        ErrorAttributes errorAttributes = new DefaultErrorAttributes();
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void getAllAvailableCustomShoppingListItems_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long habitId = 2L;

        when(customShoppingListItemService
                .findAllAvailableCustomShoppingListItems(userId, habitId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/custom/shopping-list-items/{userId}/{habitId}", userId, habitId))
                .andExpect(status().isOk());

        verify(customShoppingListItemService).findAllAvailableCustomShoppingListItems(userId, habitId);
    }

    @Test
    void getAllAvailableCustomShoppingListItems_WhenUserIdIsInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/custom/shopping-list-items/abc/1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveUserCustomShoppingListItems_ReturnsCreated() throws Exception {
        Long userId = 1L;
        Long habitAssignId = 2L;

        CustomShoppingListItemSaveRequestDto itemDto = new CustomShoppingListItemSaveRequestDto("Test item");
        BulkSaveCustomShoppingListItemDto bulkDto = new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        when(customShoppingListItemService.save(bulkDto, userId, habitAssignId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/custom/shopping-list-items/{userId}/{habitAssignId}/custom-shopping-list-items", userId, habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkDto)))
                .andExpect(status().isCreated());

        verify(customShoppingListItemService).save(bulkDto, userId, habitAssignId);
    }

    @Test
    void saveUserCustomShoppingListItems_WhenInvalidInput_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        Long habitAssignId = 2L;

        CustomShoppingListItemSaveRequestDto invalidItemDto = new CustomShoppingListItemSaveRequestDto("");
        BulkSaveCustomShoppingListItemDto invalidBulkDto = new BulkSaveCustomShoppingListItemDto(List.of(invalidItemDto));

        mockMvc.perform(post("/custom/shopping-list-items/{userId}/{habitAssignId}/custom-shopping-list-items", userId, habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBulkDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveUserCustomShoppingListItems_WithInvalidUserId_ReturnsBadRequest() throws Exception {
        Long habitAssignId = 2L;
        CustomShoppingListItemSaveRequestDto itemDto = new CustomShoppingListItemSaveRequestDto("Test item");
        BulkSaveCustomShoppingListItemDto bulkDto = new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        mockMvc.perform(post("/custom/shopping-list-items/abc/{habitAssignId}/custom-shopping-list-items", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateItemStatus_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;
        String status = "DONE";

        when(customShoppingListItemService.updateItemStatus(userId, itemId, status))
                .thenReturn(null);

        mockMvc.perform(
                        patch("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                                .param("itemId", itemId.toString())
                                .param("status", status)
                )
                .andExpect(status().isOk());

        verify(customShoppingListItemService)
                .updateItemStatus(userId, itemId, status);
    }

    @Test
    void updateItemStatus_WhenItemIdMissing_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        patch("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                                .param("status", "DONE")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemStatus_WhenUserIdInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        patch("/custom/shopping-list-items/abc/custom-shopping-list-items")
                                .param("itemId", "10")
                                .param("status", "DONE")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemStatus_WhenStatusMissing_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        mockMvc.perform(patch("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                        .param("itemId", itemId.toString()))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateItemStatusToDone_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        mockMvc.perform(
                        patch("/custom/shopping-list-items/{userId}/done", userId)
                                .param("itemId", itemId.toString())
                )
                .andExpect(status().isOk());

        verify(customShoppingListItemService)
                .updateItemStatusToDone(userId, itemId);
    }

    @Test
    void updateItemStatusToDone_WhenItemIdMissing_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        patch("/custom/shopping-list-items/{userId}/done", userId)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemStatusToDone_WhenUserIdInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        patch("/custom/shopping-list-items/abc/done")
                                .param("itemId", "10")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkDeleteCustomShoppingListItems_ReturnsOk() throws Exception {
        Long userId = 1L;
        String ids = "1,2,3";

        when(customShoppingListItemService.bulkDelete(ids))
                .thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(
                        delete("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                                .param("ids", ids)
                )
                .andExpect(status().isOk());

        verify(customShoppingListItemService).bulkDelete(ids);
    }

    @Test
    void bulkDeleteCustomShoppingListItems_WhenIdsMissing_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        delete("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkDeleteCustomShoppingListItems_WhenUserIdInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        delete("/custom/shopping-list-items/abc/custom-shopping-list-items")
                                .param("ids", "1,2")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCustomShoppingItemsByStatus_ReturnsOk() throws Exception {
        Long userId = 1L;
        String status = "ACTIVE";

        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(userId, status))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                        .param("status", status))
                .andExpect(status().isOk());

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(userId, status);
    }

    @Test
    void getAllCustomShoppingItemsByStatus_WithoutStatusParam_ReturnsOk() throws Exception {
        Long userId = 1L;

        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(userId, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId))
                .andExpect(status().isOk());

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(userId, null);
    }


    @Test
    void getAllCustomShoppingItemsByStatus_WithInvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/custom/shopping-list-items/abc/custom-shopping-list-items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCustomShoppingItemsByStatus_WithInvalidStatus_ReturnsOk() throws Exception {
        Long userId = 1L;
        String invalidStatus = "INVALID";

        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(userId, invalidStatus))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/custom/shopping-list-items/{userId}/custom-shopping-list-items", userId)
                        .param("status", invalidStatus))
                .andExpect(status().isOk());

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(userId, invalidStatus);
    }
}
