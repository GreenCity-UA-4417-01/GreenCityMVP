package greencity.mapping;

import greencity.dto.econewscomment.EcoNewsCommentVO;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EcoNewsCommentVOMapperTest {
    @InjectMocks
    EcoNewsCommentVOMapper mapper;

    @Test
    void convertTestWithoutParent() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .name("Bob")
                .role(Role.ROLE_USER)
                .build();
        EcoNews ecoNews = EcoNews.builder()
                .id(10L)
                .build();
        EcoNewsComment comment = EcoNewsComment.builder()
                .id(100L)
                .user(user)
                .ecoNews(ecoNews)
                .text("Hello")
                .deleted(false)
                .currentUserLiked(true)
                .createdDate(now)
                .modifiedDate(now)
                .usersLiked(Set.of(user))
                .parentComment(null)
                .build();
        EcoNewsCommentVO result = mapper.convert(comment);

        assertEquals(100L,result.getId());
        assertEquals("Hello",result.getText());
        assertFalse(result.isDeleted());
        assertTrue(result.isCurrentUserLiked());
        assertNull(result.getParentComment());

        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(user.getName(), result.getUser().getName());
        assertEquals(user.getRole(), result.getUser().getRole());

        assertEquals(1, result.getUsersLiked().size());
        assertEquals(ecoNews.getId(), result.getEcoNews().getId());
    }

    @Test
    void convertTestWithParent() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(2L)
                .name("Alice")
                .role(Role.ROLE_USER)
                .build();

        EcoNews ecoNews = EcoNews.builder()
                .id(20L)
                .build();
        EcoNewsComment parent = EcoNewsComment.builder()
                .id(1L)
                .user(user)
                .ecoNews(ecoNews)
                .text("Parent")
                .createdDate(now)
                .modifiedDate(now)
                .usersLiked(Set.of())
                .build();

        EcoNewsComment child = EcoNewsComment.builder()
                .id(2L)
                .user(user)
                .ecoNews(ecoNews)
                .text("Child")
                .createdDate(now)
                .modifiedDate(now)
                .parentComment(parent)
                .usersLiked(Set.of())
                .build();

        EcoNewsCommentVO result = mapper.convert(child);

        assertNotNull(result.getParentComment());
        assertEquals(1L, result.getParentComment().getId());
        assertEquals("Parent", result.getParentComment().getText());

        assertNull(result.getParentComment().getParentComment());
    }
}
