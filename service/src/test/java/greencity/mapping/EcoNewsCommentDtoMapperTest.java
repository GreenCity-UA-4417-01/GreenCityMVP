package greencity.mapping;


import greencity.dto.econewscomment.EcoNewsCommentDto;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.enums.CommentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EcoNewsCommentDtoMapperTest {
    @InjectMocks
    EcoNewsCommentDtoMapper mapper;

    @Test
    void deletedStatusTest(){

        LocalDateTime now = LocalDateTime.now();

        EcoNewsComment comment = EcoNewsComment.builder()
                .id(1l)
                .deleted(true)
                .modifiedDate(now)
                .build();
        EcoNewsCommentDto result = mapper.convert(comment);

        assertEquals(1l,result.getId());
        assertEquals(now,result.getModifiedDate());
        assertEquals(CommentStatus.DELETED,result.getStatus());

        assertNull(result.getText());
        assertNull(result.getAuthor());

    }
    @Test
    void originalStatusTest(){
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .name("Bob")
                .profilePicturePath("avatar.png")
                .build();

        EcoNewsComment comment = EcoNewsComment.builder()
                .id(2L)
                .text("Hello")
                .user(user)
                .createdDate(now)
                .modifiedDate(now)
                .usersLiked(Set.of(user))
                .currentUserLiked(true)
                .deleted(false)
                .build();

        EcoNewsCommentDto result = mapper.convert(comment);

        assertEquals(CommentStatus.ORIGINAL, result.getStatus());
        assertEquals("Hello", result.getText());
        assertEquals(1, result.getLikes());
        assertTrue(result.isCurrentUserLiked());

        assertEquals(user.getId(), result.getAuthor().getId());
        assertEquals(user.getName(), result.getAuthor().getName());
        assertEquals(user.getProfilePicturePath(),
                result.getAuthor().getUserProfilePicturePath());
    }
    @Test
    void editedStatusTest(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime modified = now.plusMinutes(10);

        User user = User.builder()
                .id(2l)
                .name("Alice")
                .profilePicturePath("picture.png")
                .build();
        EcoNewsComment comment = EcoNewsComment.builder()
                .id(3L)
                .text("Updated text")
                .user(user)
                .createdDate(now)
                .modifiedDate(modified)
                .usersLiked(Set.of())
                .currentUserLiked(false)
                .deleted(false)
                .build();
        EcoNewsCommentDto result = mapper.convert(comment);
        assertEquals(CommentStatus.EDITED, result.getStatus());
        assertEquals("Updated text", result.getText());
        assertEquals(0, result.getLikes());
        assertFalse(result.isCurrentUserLiked());

    }




}
