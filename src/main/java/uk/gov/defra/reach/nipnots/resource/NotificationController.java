package uk.gov.defra.reach.nipnots.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;
import uk.gov.defra.reach.nipnots.dto.NipNotResponse;
import uk.gov.defra.reach.nipnots.dto.ValidationResult;
import uk.gov.defra.reach.nipnots.service.NotificationService;

@RestController
public class NotificationController {
  NotificationService notificationService;

  @Autowired
  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
  public String root() {
    return "ok";
  }


  /**
   * Processes a new NipNot submission
   *
   * @param request the request
   * @return details of the newly created notification
   */
  @PostMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public NipNotResponse submitNotification(@RequestBody NipNotRequest request) {
    return notificationService.submitNotification(request);
  }

  /**
   * Returns whether the spreadsheet is valid
   *
   * @param request the request
   * @return the validation response
   */
  @PostMapping(value = "/notification/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ValidationResult validateNipnotsSpreadsheet(@RequestBody NipNotRequest request) {
    return notificationService.validate(request);
  }

  /**
   * Returns the latest notification for the authenticated user's legal entity
   *
   * @return details of the notification
   */
  @GetMapping(value = "/notification/latest")
  public NipNotResponse getLatestNotification() {
    return notificationService.getLatestNotification();
  }

}

