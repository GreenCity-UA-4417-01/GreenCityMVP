package greencity.entity.event;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "initiative_types",
    uniqueConstraints = @UniqueConstraint(name = "uk_initiative_types_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InitiativeType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "code", nullable = false, length = 30, updatable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}
