package greencity.repository.event;

import greencity.entity.event.InitiativeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface InitiativeTypeRepository extends JpaRepository<InitiativeType, Long> {
    List<InitiativeType> findByCodeIn(Collection<String> codes);
}
