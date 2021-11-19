package uk.gov.defra.reach.nipnots.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(uk.gov.defra.reach.health.ServiceHealthController.class)
public class HealthCheckConfig {

}
