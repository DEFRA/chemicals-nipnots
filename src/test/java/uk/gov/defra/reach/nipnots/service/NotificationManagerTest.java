package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.defra.reach.nipnots.client.LegalEntityDetails;
import uk.gov.defra.reach.nipnots.config.NipNotsConfig;
import uk.gov.defra.reach.nipnots.entity.NipNotsNumber;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;
import uk.gov.defra.reach.nipnots.repository.NotificationRepository;
import uk.gov.defra.reach.nipnots.repository.NotificationSubstanceRepository;

@ExtendWith(MockitoExtension.class)
class NotificationManagerTest {

  private static final LegalEntityDetails LEGAL_ENTITY_DETAILS = createLegalEntityDetails();

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationSubstanceRepository notificationSubstanceRepository;

  @Mock
  private NotificationSubstanceRowMapper notificationSubstanceRowMapper;

  @Mock
  private NipNotsConfig nipNotsConfig;

  @InjectMocks
  private NotificationManager notificationManager;

  @Captor
  private ArgumentCaptor<Notification> notificationCaptor;

  @Captor
  private ArgumentCaptor<List<NotificationSubstance>> notificationSubstanceCaptor;

  @Test
  void shouldCreateNewNotification() {
    when(nipNotsConfig.getIdPrefix()).thenReturn("TEST");
    Notification savedNotification = new Notification();
    savedNotification.setId(UUID.randomUUID());
    when(notificationRepository.save(notificationCaptor.capture())).thenReturn(savedNotification);

    List<String> rowValues1 = List.of("values1");
    List<String> rowValues2 = List.of("values2");
    when(notificationSubstanceRowMapper.mapRow(anyList())).thenAnswer(inv -> {
      List<String> values = inv.getArgument(0);
      NotificationSubstance notificationSubstance = new NotificationSubstance();
      notificationSubstance.setSubstanceName(values.get(0));
      return notificationSubstance;
    });

    Notification notification = notificationManager.saveNewNotification(LEGAL_ENTITY_DETAILS, "file name", Stream.of(rowValues1, rowValues2));

    assertThat(notification).isSameAs(savedNotification);

    Notification persistedNotification = notificationCaptor.getValue();
    assertThat(persistedNotification.getFileName()).isEqualTo("file name");
    assertThat(persistedNotification.getLegalEntityAccountId()).isEqualTo(LEGAL_ENTITY_DETAILS.getAccountId());
    assertThat(persistedNotification.getLegalEntityName()).isEqualTo(LEGAL_ENTITY_DETAILS.getName());
    assertThat(persistedNotification.getLegalEntityPostcode()).isEqualTo(LEGAL_ENTITY_DETAILS.getPostalCode());

    List<NotificationSubstance> persistedSubstances = new ArrayList<>(savedNotification.getSubstances());
    persistedSubstances.sort(Comparator.comparing(NotificationSubstance::getSubstanceName));
    assertThat(persistedSubstances.get(0).getSubstanceName()).isEqualTo(rowValues1.get(0));
    assertThat(persistedSubstances.get(1).getSubstanceName()).isEqualTo(rowValues2.get(0));
  }

  @Test
  void shouldUpdatePreviousNotificationsToBeInactive() {
    Notification previousNotification = new Notification();
    previousNotification.setReferenceNumber("TEST-30-1234567890-3-0001");
    previousNotification.setActive(true);

    Notification savedNotification = new Notification();
    savedNotification.setId(UUID.randomUUID());
    when(notificationRepository.save(notificationCaptor.capture())).thenReturn(savedNotification);

    when(notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(LEGAL_ENTITY_DETAILS.getAccountId())).thenReturn(Optional.of(previousNotification));

    Notification notification = notificationManager.saveNewNotification(LEGAL_ENTITY_DETAILS, "file name", Stream.empty());
    assertThat(notification).isSameAs(savedNotification);

    assertThat(previousNotification.isActive()).isFalse();
    assertThat(notificationCaptor.getValue().getReferenceNumber()).isEqualTo("TEST-30-1234567890-3-0002");
  }

  @Test
  void shouldCreateUniqueReferenceNumber() {
    when(nipNotsConfig.getIdPrefix()).thenReturn("TEST");

    Notification savedNotification = new Notification();
    savedNotification.setId(UUID.randomUUID());
    when(notificationRepository.save(notificationCaptor.capture())).thenReturn(savedNotification);
    when(notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(LEGAL_ENTITY_DETAILS.getAccountId())).thenReturn(Optional.empty());

    ArgumentCaptor<String> leCodeCaptor = ArgumentCaptor.forClass(String.class);
    when(notificationRepository.existsByReferenceNumberContains(leCodeCaptor.capture())).thenReturn(true, true, false);

    notificationManager.saveNewNotification(LEGAL_ENTITY_DETAILS, "file name", Stream.empty());

    verify(notificationRepository, times(3)).existsByReferenceNumberContains(anyString());

    Notification persistedNotification = notificationCaptor.getValue();
    assertThat(NipNotsNumber.fromString(persistedNotification.getReferenceNumber()).getLeCode()).isEqualTo(leCodeCaptor.getAllValues().get(2));
  }

  @Test
  void shouldGetLatestNotificationForLegalEntity() {
    UUID legalEntityId = UUID.randomUUID();
    Notification notification = new Notification();
    when(notificationRepository.findByLegalEntityAccountIdAndActiveIsTrue(legalEntityId)).thenReturn(Optional.of(notification));

    Optional<Notification> result = notificationManager.getLatestNotificationForLegalEntity(legalEntityId);
    assertThat(result).contains(notification);
  }


  private static LegalEntityDetails createLegalEntityDetails() {
    LegalEntityDetails legalEntityDetails = new LegalEntityDetails();
    legalEntityDetails.setPostalCode("LE POSTCODE");
    legalEntityDetails.setName("LE NAME");
    legalEntityDetails.setAccountId(UUID.randomUUID());
    return legalEntityDetails;
  }
}
