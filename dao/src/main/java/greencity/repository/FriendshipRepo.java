package greencity.repository;

import greencity.entity.Friendship;
import greencity.entity.FriendshipId;
import greencity.entity.User;
import greencity.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, FriendshipId> {
    /**
     * Method for counting accepted friends of a user.
     *
     * @param userId {@link User} id
     * @param status {@link FriendshipStatus} status
     * @return number of {@link Friendship} friends.
     */
    Long countByUserIdAndStatus(Long userId, FriendshipStatus status);
}