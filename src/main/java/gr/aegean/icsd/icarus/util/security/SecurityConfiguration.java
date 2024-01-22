package gr.aegean.icsd.icarus.util.security;

import gr.aegean.icsd.icarus.util.security.oauth2.Auth0AuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static jakarta.servlet.DispatcherType.*;


/**
 * Configuration class defining the necessary options for application wide security configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableJpaAuditing
public class SecurityConfiguration {


    @Value("${security.httpBasic.enableHttpBasic}")
    private boolean enableHttpBasic;



    /**
     * Initializes and configures the filter chain
     *
     * @param http HttpSecurity object
     *
     * @return Fully configured Filter chain
     *
     * @throws Exception If invalid configuration is provided
     */
    @Bean
    SecurityFilterChain createFilterChain(HttpSecurity http, Auth0AuthenticationConverter converter) throws Exception {

        http

                .authorizeHttpRequests(authorize -> authorize

                        .dispatcherTypeMatchers(FORWARD, ERROR, INCLUDE).permitAll()

                        // Authentication endpoints
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers("/").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v0/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v0/users/reset**").permitAll()

                        .requestMatchers("/api/v0/users/{username}/accounts/**").authenticated()
                        .requestMatchers("/api/v0/tests/**").authenticated()
                        .requestMatchers("/api/v0/functions/**").authenticated()
                        .requestMatchers("/api/v0/users/**").authenticated()
                )

                .headers(headers -> headers
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)

                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )

                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'")
                        )

                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
                        )

                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)
                        )
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .csrf(AbstractHttpConfigurer::disable)

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))

                );

        if (enableHttpBasic) {
               http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }


    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }


}
