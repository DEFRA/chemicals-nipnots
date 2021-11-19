package uk.gov.defra.reach.nipnots.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.gov.defra.reach.nipnots.security.JwtAuthenticationEntryPoint;
import uk.gov.defra.reach.nipnots.security.JwtAuthenticationFilter;
import uk.gov.defra.reach.nipnots.security.JwtTokenValidator;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private JwtAuthenticationEntryPoint unauthorizedHandler;

  private JwtTokenValidator jwtTokenValidator;

  @Autowired
  public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtTokenValidator jwtTokenValidator) {
    this.unauthorizedHandler = unauthorizedHandler;
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // "/" endpoint is left unauthenticated by only authenticating endpoints that have at least 1 character
    web.ignoring().antMatchers("/healthcheck");
  }


  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
        .and().addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/").permitAll()
        .anyRequest().authenticated();

  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenValidator);
  }

}
