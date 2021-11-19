package uk.gov.defra.reach.nipnots.config;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbConfiguration {

  @Bean
  public PhysicalNamingStrategy physicalNamingStrategy() {
    return new PhysicalNamingStrategy() {
      @Override
      public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return capitalize(name);
      }

      @Override
      public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return capitalize(name);
      }

      @Override
      public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return capitalize(name);
      }

      @Override
      public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return capitalize(name);
      }

      @Override
      public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return capitalize(name);
      }

      private Identifier capitalize(Identifier original) {
        if (original != null) {
          return Identifier.toIdentifier(StringUtils.capitalize(original.getText()));
        }
        return null;
      }
    };
  }

}
