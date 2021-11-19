package uk.gov.defra.reach.nipnots.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.defra.reach.nipnots.client.AuditClient;
import uk.gov.defra.reach.nipnots.client.NotifyClient;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.mapper.NipNotsConfirmationBuilder;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.exceptions.UserMissingContactIdException;

@Service
@Slf4j
public class NotifyService {

  private final NipNotsConfirmationBuilder nipNotsConfirmationBuilder;

  private final NotifyClient notifyClient;

  private final AuditClient auditClient;

  private static final String ERROR_SUBMISSION_REQUIRES_CONTACT_ID = "Upload NIPNOTS spreadsheet requires contactId";

  private static final String SEND_NOTIFICATION_ERROR = "Could not send email notification to {}";

  private static final String UPDATED = "updated";

  private static final String SUBMITTED = "submitted";

  @Autowired
  public NotifyService(NipNotsConfirmationBuilder nipNotsConfirmationBuilder, NotifyClient notifyClient, AuditClient auditClient) {
    this.nipNotsConfirmationBuilder = nipNotsConfirmationBuilder;
    this.notifyClient = notifyClient;
    this.auditClient = auditClient;
  }

  /**
   * Sends a confirmation email to the user once the nipnots submission process is completed
   *
   * @param user details of the current user
   * @param data object containing nipnots spreadsheet data
   * @param isNewSubmission flag to determine whether the submission is a new spreadsheet or a re-upload
   */
  public void sendNipnotConfirmationEmail(AuthenticatedUser user, Notification data, boolean isNewSubmission) {
    String emailAddress = user.getUser().getEmailAddress().orElseThrow();
    String action = isNewSubmission ? SUBMITTED : UPDATED;
    EmailNotification notification = nipNotsConfirmationBuilder.create(emailAddress, data, action);

    HttpStatus responseStatus = notifyClient.sendNotification(notification);

    if (responseStatus != HttpStatus.CREATED) {
      log.error(SEND_NOTIFICATION_ERROR, emailAddress);
    }

    if (user.getUser().getContactId().isEmpty()) {
      throw new UserMissingContactIdException(ERROR_SUBMISSION_REQUIRES_CONTACT_ID);
    }

    auditClient.auditNotificationEvent(user, notification, responseStatus.value());
  }
}
