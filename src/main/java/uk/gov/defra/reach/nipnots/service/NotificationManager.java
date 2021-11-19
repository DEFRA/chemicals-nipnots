package uk.gov.defra.reach.nipnots.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;
import uk.gov.defra.reach.nipnots.client.LegalEntityDetails;
import uk.gov.defra.reach.nipnots.config.NipNotsConfig;
import uk.gov.defra.reach.nipnots.entity.NipNotsNumber;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.repository.NotificationRepository;

/**
 * Responsible for the creation, persistence and retrieval of Notifications.
 */
@Component
@Slf4j
public class NotificationManager {

  private final NipNotsConfig nipNotsConfig;

  private final NotificationRepository notificationRepository;

  private final NotificationSubstanceRowMapper notificationSubstanceRowMapper;

  public NotificationManager(NipNotsConfig nipNotsConfig, NotificationRepository notificationRepository,
      NotificationSubstanceRowMapper notificationSubstanceRowMapper) {
    this.nipNotsConfig = nipNotsConfig;
    this.notificationRepository = notificationRepository;
    this.notificationSubstanceRowMapper = notificationSubstanceRowMapper;
  }

  @Transactional
  public Notification saveNewNotification(LegalEntityDetails legalEntity, String fileName, Stream<List<String>> substances) {
    Optional<Notification> existingNotification = notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(legalEntity.getAccountId());

    existingNotification.ifPresent(notification -> notification.setActive(false));

    NipNotsNumber nipNotsNumber = generateReferenceNumber(existingNotification);

    Notification notification = createNotification(legalEntity, fileName, nipNotsNumber);
    notification = notificationRepository.save(notification);
    notification.setActive(true);

    substances
        .map(notificationSubstanceRowMapper::mapRow)
        .forEach(notification::addSubstance);

    return notification;
  }

  public Optional<Notification> getLatestNotificationForLegalEntity(UUID legalEntityId) {
    return notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(legalEntityId);
  }

  private NipNotsNumber generateReferenceNumber(Optional<Notification> existingNotification) {
    return existingNotification.map(notification -> {
      NipNotsNumber existingReference = NipNotsNumber.fromString(notification.getReferenceNumber());
      return NipNotsNumber.builder()
          .prefix(existingReference.getPrefix())
          .leCode(existingReference.getLeCode())
          .index(existingReference.getIndex() + 1)
          .build();
    }).orElseGet(() -> {
      long leNumber = getUniqueLeId();
      return NipNotsNumber.builder()
          .prefix(nipNotsConfig.getIdPrefix())
          .leCode(String.valueOf(leNumber))
          .index(1)
          .build();
    });
  }

  private long getUniqueLeId() {
    long leNumber = RandomUtils.nextLong(0L, 10000000000L);
    while (notificationRepository.existsByReferenceNumberContains(String.valueOf(leNumber))) {
      leNumber = RandomUtils.nextLong(0L, 10000000000L);
    }
    return leNumber;
  }

  private Notification createNotification(LegalEntityDetails legalEntity, String fileName, NipNotsNumber nipNotsNumber) {
    Notification notification = new Notification();
    notification.setLegalEntityAccountId(legalEntity.getAccountId());
    notification.setLegalEntityName(legalEntity.getName());
    notification.setLegalEntityPostcode(legalEntity.getPostalCode());
    notification.setFileName(fileName);
    notification.setCreatedAt(Instant.now());
    notification.setReferenceNumber(nipNotsNumber.toString());
    return notification;
  }
}
