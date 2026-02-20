package greencity.entity.event;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "event_images",
    indexes = @Index(name = "idx_event_images_event_id", columnList = "event_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_event_images_event"))
    private Event event;

    @Column(name = "is_main", nullable = false)
    private boolean main = false;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType; // must be image/jpeg or image/png (DB check)

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private EventImageData data;
}
