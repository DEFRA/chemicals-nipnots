package uk.gov.defra.reach.nipnots.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;

/**
 * A single substance from within a NipNot Notification
 */
@Data
@Entity
public class NotificationSubstance {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "NotificationSubstanceId")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "NotificationId")
  private Notification notification;

  //Substance Name
  private String substanceName;

  //EC number
  private String ecNumber;

  //CAS number
  private String casNumber;

  //ECHA Registration Number
  private String echaRegistrationNumber;

  //IUPAC name
  private String iupacName;

  //Other names (usual name, trade name, abbreviation)
  private String otherNames;

  // CAS name
  private String casName;

  //Other identity codes
  private String otherIdentityCodes;

  //Is this substance subject to authorisation?
  private String substanceSubjectToAuthorisation;

  //Is this substance subject to restriction?
  private String substanceSubjectToRestriction;

  //Additional information relevant to appropriate risk management
  private String additionalInformationRelevantToAppropriateRiskManagement;

  //Molecular formula
  private String molecularFormula;

  //Structural formula (including SMILES or InChi  notation, if available)
  private String structuralFormula;

  //Information on optical activity and typical ratio of (stereo) isomers (if applicable and appropriate)
  private String informationOnOpticalActivityAndTypicalRatioOfIsomers;

  //Molecular weight or molecular weight range
  private String molecularWeight;

  //Degree of purity ( %)
  private String degreeOfPurity;

  //Nature of impurities, including isomers and by-products
  private String natureOfImpurities;

  //Percentage of (significant) main impurities
  private String percentageOfMainImpurities;

  //Nature and order of magnitude (… ppm, … %) of any additives (e.g. stabilising agents or inhibitors)
  private String natureAndOrderOfMagnitudeOfAnyAdditives;

  //Spectral data (ultra-violet, infra-red, nuclear magnetic resonance or mass spectrum)
  private String spectralData;

  //High-pressure liquid chromatogram, gas chromatogram
  private String highPressureLiquidChromatogramGasChromatogram;

  //Description of the analytical methods for the identification of the substance, impurities and additives.
  private String analyticalMethodsForIdentificationOfSubstanceImpuritiesAndAdditives;

  //Tonnage band
  private String tonnageBand;

}
