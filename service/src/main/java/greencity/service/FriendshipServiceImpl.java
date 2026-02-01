package greencity.service;

import greencity.enums.FriendshipStatus;
import greencity.repository.FriendshipRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipRepo friendshipRepo;

    @Override
    public Long countByUserIdAndStatus(Long userId) {
        return friendshipRepo.countByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
    }
}
