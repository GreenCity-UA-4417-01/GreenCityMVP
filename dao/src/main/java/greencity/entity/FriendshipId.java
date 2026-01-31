package greencity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FriendshipId implements Serializable {
    @Column(name = "user_id")
    private Long user;

    @Column(name = "friend_id")
    private Long friend;
}