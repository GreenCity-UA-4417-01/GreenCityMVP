package greencity.service;

import greencity.repository.FriendshipRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {

    @Mock
    private FriendshipRepo friendshipRepo;

    @InjectMocks
    private FriendshipServiceImpl friendshipServiceImpl;

    @Test
    void countByUserId_shouldReturnFriendshipCount() {
        Long expectedFriendshipCount = 1L;
        Long userId = 1L;

        when(friendshipRepo.countByUserId(userId)).thenReturn(expectedFriendshipCount);

        Long actualFriendshipCount = friendshipServiceImpl.countByUserId(userId);

        verify(friendshipRepo).countByUserId(userId);
        assertEquals(expectedFriendshipCount, actualFriendshipCount);
    }
}
