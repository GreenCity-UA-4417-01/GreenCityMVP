package greencity.service;

public interface FriendshipService {
    /**
     * Method for Counting friendships of a user.
     *
     * @param userId the user's id
     * @return number of friendships
     */
    Long countByUserIdAndStatus(Long userId);
}
