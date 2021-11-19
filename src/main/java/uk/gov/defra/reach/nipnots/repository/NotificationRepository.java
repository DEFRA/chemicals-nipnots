package uk.gov.defra.reach.nipnots.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import uk.gov.defra.reach.nipnots.entity.Notification;

public interface NotificationRepository extends CrudRepository<Notification, UUID> {

  Optional<Notification> findByLegalEntityAccountIdAndActiveIsTrue(UUID legalEntityId);

  boolean existsByReferenceNumberContains(String leCode);
}
