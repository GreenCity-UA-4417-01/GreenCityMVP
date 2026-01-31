package greencity.entity.event;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "event_date_time_locations",
    indexes = @Index(name = "idx_edtl_event_id", columnList = "event_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventDateTimeLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_edtl_event"))
    private Event event;

    @Column(name = "start_date_time", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime endDateTime;

    @Column(name = "all_day", nullable = false)
    private boolean allDay = false;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "online_link", length = 2048)
    private String onlineLink;
}
