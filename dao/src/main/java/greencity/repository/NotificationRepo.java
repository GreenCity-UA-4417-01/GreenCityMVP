package greencity.repository;

import greencity.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverIdOrderByCreationDateDesc(Long receiverId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :id AND n.receiver.id = :receiverId")
    int deleteByIdAndReceiverId(@Param("id") Long id, @Param("receiverId") Long receiverId);
}