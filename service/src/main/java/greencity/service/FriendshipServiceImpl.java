package greencity.service;

import greencity.repository.FriendshipRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipRepo friendshipRepo;

    @Override
    public Long countByUserId(Long userId) {
        return friendshipRepo.countByUserId(userId);
    }
}
