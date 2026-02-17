package greencity.repository;

import greencity.entity.Friendship;
import greencity.entity.FriendshipId;
import greencity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, FriendshipId> {
    /**
     * Method for counting accepted friends of a user.
     *
     * @param userId {@link User} id
     * @return number of {@link Friendship} friends.
     */
    Long countByUserId(Long userId);
}