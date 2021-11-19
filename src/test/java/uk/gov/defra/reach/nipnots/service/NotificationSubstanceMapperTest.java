package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;

class NotificationSubstanceMapperTest {

  private final NotificationSubstanceRowMapper notificationSubstanceRowMapper = new NotificationSubstanceRowMapper();

  @Test
  void shouldMapValuesToFields() {
    List<String> rowValues = buildValues();
    NotificationSubstance notificationSubstance = notificationSubstanceRowMapper.mapRow(rowValues);

    assertThat(notificationSubstance.getSubstanceName()).isEqualTo("value1");
    assertThat(notificationSubstance.getEcNumber()).isEqualTo("value2");
    assertThat(notificationSubstance.getCasNumber()).isEqualTo("value3");
    assertThat(notificationSubstance.getEchaRegistrationNumber()).isEqualTo("value4");
    assertThat(notificationSubstance.getIupacName()).isEqualTo("value5");
    assertThat(notificationSubstance.getOtherNames()).isEqualTo("value6");
    assertThat(notificationSubstance.getCasName()).isEqualTo("value7");
    assertThat(notificationSubstance.getOtherIdentityCodes()).isEqualTo("value8");
    assertThat(notificationSubstance.getSubstanceSubjectToAuthorisation()).isEqualTo("value9");
    assertThat(notificationSubstance.getSubstanceSubjectToRestriction()).isEqualTo("value10");
    assertThat(notificationSubstance.getAdditionalInformationRelevantToAppropriateRiskManagement()).isEqualTo("value11");
    assertThat(notificationSubstance.getMolecularFormula()).isEqualTo("value12");
    assertThat(notificationSubstance.getStructuralFormula()).isEqualTo("value13");
    assertThat(notificationSubstance.getInformationOnOpticalActivityAndTypicalRatioOfIsomers()).isEqualTo("value14");
    assertThat(notificationSubstance.getMolecularWeight()).isEqualTo("value15");
    assertThat(notificationSubstance.getDegreeOfPurity()).isEqualTo("value16");
    assertThat(notificationSubstance.getNatureOfImpurities()).isEqualTo("value17");
    assertThat(notificationSubstance.getPercentageOfMainImpurities()).isEqualTo("value18");
    assertThat(notificationSubstance.getNatureAndOrderOfMagnitudeOfAnyAdditives()).isEqualTo("value19");
    assertThat(notificationSubstance.getSpectralData()).isEqualTo("value20");
    assertThat(notificationSubstance.getHighPressureLiquidChromatogramGasChromatogram()).isEqualTo("value21");
    assertThat(notificationSubstance.getAnalyticalMethodsForIdentificationOfSubstanceImpuritiesAndAdditives()).isEqualTo("value22");
    assertThat(notificationSubstance.getTonnageBand()).isEqualTo("value23");
  }

  private static List<String> buildValues() {
    return IntStream.range(1, 24).mapToObj(i -> "value" + i).collect(Collectors.toList());
  }

}
