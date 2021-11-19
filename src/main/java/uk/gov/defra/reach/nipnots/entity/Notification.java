package uk.gov.defra.reach.nipnots.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

@Entity
@Data
@EqualsAndHashCode(exclude = "substances")
@ToString(exclude = "substances")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "NotificationId")
  @Type(type = "uuid-char")
  private UUID id;

  private UUID legalEntityAccountId;

  private String legalEntityName;

  private String legalEntityPostcode;

  private Instant createdAt;

  private String fileName;

  private String referenceNumber;

  private boolean active;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,  mappedBy = "notification")
  @LazyCollection(LazyCollectionOption.EXTRA)
  private Set<NotificationSubstance> substances = new HashSet<>();

  public void addSubstance(NotificationSubstance substance) {
    substance.setNotification(this);
    substances.add(substance);
  }

}
