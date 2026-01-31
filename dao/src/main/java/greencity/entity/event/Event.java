package greencity.entity.event;

import greencity.entity.User;
import greencity.enums.InviteScope;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_events_organizer_id", columnList = "organizer_id"),
        @Index(name = "idx_events_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_events_organizer"))
    private User organizer;

    @Column(name = "title", nullable = false, length = 70)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_scope", nullable = false, length = 10)
    private InviteScope inviteScope = InviteScope.ALL;

    /**
     * DB sets NOW() (defaultValueComputed), so we treat it as DB-generated.
     */
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "events_initiative_types",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "initiative_type_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "pk_events_initiative_types",
            columnNames = {"event_id", "initiative_type_id"}))
    private Set<InitiativeType> initiativeTypes = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventDateTimeLocation> dateTimeLocations = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventImage> images = new HashSet<>();
}
