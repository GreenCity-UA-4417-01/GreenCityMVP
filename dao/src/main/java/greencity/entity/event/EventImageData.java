package greencity.entity.event;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_image_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventImageData {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "image_id")
    private Long imageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "image_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_event_image_data_image"))
    private EventImage image;

    @Column(name = "data", nullable = false)
    private byte[] data;
}
