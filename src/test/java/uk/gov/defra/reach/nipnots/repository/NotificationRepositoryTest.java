package uk.gov.defra.reach.nipnots.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.defra.reach.nipnots.entity.Notification;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-dev.properties")
@DataJpaTest
class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Test
  void shouldSaveAndRetrieve() {
    Notification notification = createNotification(UUID.randomUUID());

    Notification savedNotification = notificationRepository.save(notification);
    assertThat(savedNotification.getId()).isNotNull();

    Notification retrievedNotification = notificationRepository.findById(savedNotification.getId()).get();

    assertThat(retrievedNotification.getId()).isEqualTo(savedNotification.getId());
    assertThat(retrievedNotification.getLegalEntityAccountId()).isEqualTo(notification.getLegalEntityAccountId());
    assertThat(retrievedNotification.getLegalEntityName()).isEqualTo(notification.getLegalEntityName());
    assertThat(retrievedNotification.getLegalEntityPostcode()).isEqualTo(notification.getLegalEntityPostcode());
    assertThat(retrievedNotification.getReferenceNumber()).isEqualTo(notification.getReferenceNumber());
    assertThat(retrievedNotification.getFileName()).isEqualTo(notification.getFileName());
    assertThat(retrievedNotification.isActive()).isEqualTo(notification.isActive());
  }

  @Test
  void shouldFindActiveNotificationForLe() {
    UUID legalEntityId = UUID.randomUUID();

    assertThat(notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(legalEntityId)).isEmpty();

    Notification notification1 = createNotification(legalEntityId);
    Notification notification2 = createNotification(legalEntityId);

    notification1.setActive(false);
    notification2.setActive(true);

    notification1 = notificationRepository.save(notification1);
    notification2 = notificationRepository.save(notification2);

    Optional<Notification> activeNotification = notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(legalEntityId);
    assertThat(activeNotification).isPresent().map(Notification::getId).contains(notification2.getId());
  }

  private static Notification createNotification(UUID legalEntityId) {
    Notification notification = new Notification();
    notification.setLegalEntityAccountId(legalEntityId);
    notification.setLegalEntityName("le name");
    notification.setLegalEntityPostcode("LE1 1LE");
    notification.setReferenceNumber("ref1");
    notification.setActive(true);
    notification.setFileName("filename.xlsx");
    notification.setCreatedAt(Instant.now());
    return notification;
  }

}
