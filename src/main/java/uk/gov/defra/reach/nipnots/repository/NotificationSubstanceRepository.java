package uk.gov.defra.reach.nipnots.repository;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;

public interface NotificationSubstanceRepository extends CrudRepository<NotificationSubstance, UUID> {

}
