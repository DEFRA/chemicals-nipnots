package uk.gov.defra.reach.nipnots.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-dev.properties")
@DataJpaTest
class NotificationSubstanceRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private NotificationSubstanceRepository notificationSubstanceRepository;

  @Test
  void shouldSave() {
    Notification notification = createNotification();

    assertThat(notificationSubstanceRepository.save(createSubstance(notification)).getId()).isNotNull();
    assertThat(notificationSubstanceRepository.save(createSubstance(notification)).getId()).isNotNull();
    assertThat(notificationSubstanceRepository.save(createSubstance(notification)).getId()).isNotNull();

    assertThat(notificationSubstanceRepository.findAll()).hasSize(3);
  }

  @Test
  void shouldRetrieve() {
    Notification notification = createNotification();
    NotificationSubstance substance = createSubstance(notification);
    UUID substanceId = notificationSubstanceRepository.save(substance).getId();

    NotificationSubstance retrieved = notificationSubstanceRepository.findById(substanceId).get();

    assertThat(retrieved.getSubstanceName()).isEqualTo(substance.getSubstanceName());
    assertThat(retrieved.getEcNumber()).isEqualTo(substance.getEcNumber());
    assertThat(retrieved.getCasNumber()).isEqualTo(substance.getCasNumber());
    assertThat(retrieved.getEchaRegistrationNumber()).isEqualTo(substance.getEchaRegistrationNumber());
    assertThat(retrieved.getIupacName()).isEqualTo(substance.getIupacName());
    assertThat(retrieved.getOtherNames()).isEqualTo(substance.getOtherNames());
    assertThat(retrieved.getCasName()).isEqualTo(substance.getCasName());
    assertThat(retrieved.getOtherIdentityCodes()).isEqualTo(substance.getOtherIdentityCodes());
    assertThat(retrieved.getSubstanceSubjectToAuthorisation()).isEqualTo(substance.getSubstanceSubjectToAuthorisation());
    assertThat(retrieved.getSubstanceSubjectToRestriction()).isEqualTo(substance.getSubstanceSubjectToRestriction());
    assertThat(retrieved.getAdditionalInformationRelevantToAppropriateRiskManagement()).isEqualTo(substance.getAdditionalInformationRelevantToAppropriateRiskManagement());
    assertThat(retrieved.getMolecularFormula()).isEqualTo(substance.getMolecularFormula());
    assertThat(retrieved.getStructuralFormula()).isEqualTo(substance.getStructuralFormula());
    assertThat(retrieved.getInformationOnOpticalActivityAndTypicalRatioOfIsomers()).isEqualTo(substance.getInformationOnOpticalActivityAndTypicalRatioOfIsomers());
    assertThat(retrieved.getMolecularWeight()).isEqualTo(substance.getMolecularWeight());
    assertThat(retrieved.getDegreeOfPurity()).isEqualTo(substance.getDegreeOfPurity());
    assertThat(retrieved.getNatureOfImpurities()).isEqualTo(substance.getNatureOfImpurities());
    assertThat(retrieved.getPercentageOfMainImpurities()).isEqualTo(substance.getPercentageOfMainImpurities());
    assertThat(retrieved.getNatureAndOrderOfMagnitudeOfAnyAdditives()).isEqualTo(substance.getNatureAndOrderOfMagnitudeOfAnyAdditives());
    assertThat(retrieved.getSpectralData()).isEqualTo(substance.getSpectralData());
    assertThat(retrieved.getHighPressureLiquidChromatogramGasChromatogram()).isEqualTo(substance.getHighPressureLiquidChromatogramGasChromatogram());
    assertThat(retrieved.getAnalyticalMethodsForIdentificationOfSubstanceImpuritiesAndAdditives()).isEqualTo(substance.getAnalyticalMethodsForIdentificationOfSubstanceImpuritiesAndAdditives());
    assertThat(retrieved.getTonnageBand()).isEqualTo(substance.getTonnageBand());
  }

  private Notification createNotification() {
    Notification notification = new Notification();
    notification.setLegalEntityAccountId(UUID.randomUUID());
    notification.setLegalEntityName("le name");
    notification.setLegalEntityPostcode("LE1 1LE");
    notification.setReferenceNumber("ref1");
    notification.setActive(true);
    notification.setFileName("filename.xlsx");
    notification.setCreatedAt(Instant.now());
    return notificationRepository.save(notification);
  }

  @SneakyThrows
  private NotificationSubstance createSubstance(Notification notification) {
    NotificationSubstance notificationSubstance = new NotificationSubstance();
    notificationSubstance.setNotification(notification);
    for (Field field : NotificationSubstance.class.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.getType().equals(String.class)) {
        field.set(notificationSubstance, RandomStringUtils.randomAlphanumeric(100));
      }
    }
    return notificationSubstance;
  }


}
