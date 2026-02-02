package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserVO;
import greencity.service.FriendshipService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FriendshipControllerTest {
    private MockMvc mockMvc;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FriendshipController friendshipController;

    private static ObjectMapper objectMapper;

    private final Principal principal = getPrincipal();

    @BeforeAll
    static void initObjectMapper() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(friendshipController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper))
                .build();
    }

    @Test
    void getCountOfFriendshipsForUser() throws Exception {
        UserVO userVO = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(friendshipService.countByUserIdAndStatus(userVO.getId())).thenReturn(1L);

        mockMvc.perform(get("/friendship/getCountOfFriendships")
                .principal(principal)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendshipCount").value(1));

        verify(friendshipService).countByUserIdAndStatus(userVO.getId());
    }
}