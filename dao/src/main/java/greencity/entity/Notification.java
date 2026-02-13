package greencity.entity;

import greencity.enums.NotificationSource;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private NotificationSource source;

    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate;
}