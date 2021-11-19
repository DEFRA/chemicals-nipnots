package uk.gov.defra.reach.nipnots.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;
import uk.gov.defra.reach.nipnots.dto.NipNotResponse;
import uk.gov.defra.reach.nipnots.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @InjectMocks
  private NotificationController notificationController;

  @Mock
  private NotificationService notificationService;

  @Test
  void submitNotification_returnsResponse() {
    NipNotRequest request = new NipNotRequest(UUID.randomUUID(), "filename");
    NipNotResponse response = NipNotResponse.builder().build();
    when(notificationService.submitNotification(request)).thenReturn(response);

    NipNotResponse result = notificationController.submitNotification(request);

    assertThat(result).isSameAs(response);
  }

  @Test
  void getLatestNotification_returnsLatestNotification() {
    NipNotResponse response = NipNotResponse.builder().build();
    when(notificationService.getLatestNotification()).thenReturn(response);

    NipNotResponse result = notificationController.getLatestNotification();

    assertThat(result).isSameAs(response);
  }

  @Test
  void healthCheck_returnsOk() {
    assertThat(notificationController.root()).isEqualTo("ok");
  }

}
