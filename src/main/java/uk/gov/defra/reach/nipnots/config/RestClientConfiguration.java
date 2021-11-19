package uk.gov.defra.reach.nipnots.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.defra.reach.nipnots.filter.HeaderInterceptor;

@Configuration
public class RestClientConfiguration {

  @Value("${reach.file-service.url}")
  private String fileServiceUrl;

  @Value("${reach.audit.url}")
  private String auditUrl;

  @Value("${reach.url}")
  private String reachUrl;

  @Value("${reach.notify.url}")
  private String notifyUrl;

  @Value("${reach.monitoring.url}")
  private String monitoringUrl;

  @Bean
  public RestTemplate fileServiceRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new HeaderInterceptor());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(fileServiceUrl));
    return restTemplate;
  }

  @Bean
  public RestTemplate auditRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new HeaderInterceptor());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(auditUrl));
    return restTemplate;
  }

  @Bean
  public RestTemplate reachRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new HeaderInterceptor());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(reachUrl));
    return restTemplate;
  }

  @Bean
  public RestTemplate notifyRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new HeaderInterceptor());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(notifyUrl));
    return restTemplate;
  }

  @Bean
  public RestTemplate monitoringRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new HeaderInterceptor());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(monitoringUrl));
    return restTemplate;
  }
}
