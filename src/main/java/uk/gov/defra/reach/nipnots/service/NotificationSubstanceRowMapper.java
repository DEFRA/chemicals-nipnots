package uk.gov.defra.reach.nipnots.service;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;

/**
 * Mapper to convert from an indexed list of values extracted from a spreadsheet row to a {@link NotificationSubstance}
 */
@Component
public class NotificationSubstanceRowMapper {

  /**
   * Maps a list of values to a {@code NotificationSubstance}
   *
   * @param rowValues the list of values
   * @return a new NotificationSubstance
   */
  public NotificationSubstance mapRow(List<String> rowValues) {
    NotificationSubstance notificationSubstance = new NotificationSubstance();
    notificationSubstance.setSubstanceName(rowValues.get(0));
    notificationSubstance.setEcNumber(rowValues.get(1));
    notificationSubstance.setCasNumber(rowValues.get(2));
    notificationSubstance.setEchaRegistrationNumber(rowValues.get(3));
    notificationSubstance.setIupacName(rowValues.get(4));
    notificationSubstance.setOtherNames(rowValues.get(5));
    notificationSubstance.setCasName(rowValues.get(6));
    notificationSubstance.setOtherIdentityCodes(rowValues.get(7));
    notificationSubstance.setSubstanceSubjectToAuthorisation(rowValues.get(8));
    notificationSubstance.setSubstanceSubjectToRestriction(rowValues.get(9));
    notificationSubstance.setAdditionalInformationRelevantToAppropriateRiskManagement(rowValues.get(10));
    notificationSubstance.setMolecularFormula(rowValues.get(11));
    notificationSubstance.setStructuralFormula(rowValues.get(12));
    notificationSubstance.setInformationOnOpticalActivityAndTypicalRatioOfIsomers(rowValues.get(13));
    notificationSubstance.setMolecularWeight(rowValues.get(14));
    notificationSubstance.setDegreeOfPurity(rowValues.get(15));
    notificationSubstance.setNatureOfImpurities(rowValues.get(16));
    notificationSubstance.setPercentageOfMainImpurities(rowValues.get(17));
    notificationSubstance.setNatureAndOrderOfMagnitudeOfAnyAdditives(rowValues.get(18));
    notificationSubstance.setSpectralData(rowValues.get(19));
    notificationSubstance.setHighPressureLiquidChromatogramGasChromatogram(rowValues.get(20));
    notificationSubstance.setAnalyticalMethodsForIdentificationOfSubstanceImpuritiesAndAdditives(rowValues.get(21));
    notificationSubstance.setTonnageBand(rowValues.get(22));

    return notificationSubstance;
  }

}
