package gr.aegean.icsd.icarus.util.security;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.util.exceptions.IcarusConfigurationException;
import gr.aegean.icsd.icarus.util.security.httpbasic.SqlAuthenticationManager;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
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


    public static int CREDENTIALS_EXPIRATION_PERIOD;
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final int MAXIMUM_PASSWORD_LENGTH = 150;


    @Value("${security.users.enableTestUser}")
    private boolean enableTestAccounts;

    @Value("${security.users.testUserUsername}")
    private String testAccountUsername;

    @Value("${security.users.testUserPassword}")
    private String testAccountPassword;

    @Value("${security.users.testUserEmail}")
    private String testAccountEmail;


    @Value("${security.credentialsExpirationPeriod}")
    public void setCredentialsExpirationPeriod(Integer credentialsExpirationPeriod) {

        if (credentialsExpirationPeriod != null && credentialsExpirationPeriod > 0) {
            SecurityConfiguration.CREDENTIALS_EXPIRATION_PERIOD = credentialsExpirationPeriod;
        }
        else {
            throw new IcarusConfigurationException("Credentials expiration period cannot be: "
                    + credentialsExpirationPeriod);
        }
    }



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
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }


    @Bean
    DelegatingPasswordEncoder createEncoder() {

        DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return passwordEncoder;
    }


    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }


    /**
     * If test accounts are enabled, this method creates them
     * ( provided that they do not already exist in the database )
     *
     * @param userManager User manager that will be used to check for the account's existence
     *
     * @return true if the accounts were created
     */
    @Bean
    boolean createTestUsers(SqlAuthenticationManager userManager) {

        if (enableTestAccounts && !userManager.userExists(testAccountUsername)) {

            UserDetails testJournalist = new IcarusUser(testAccountUsername, testAccountPassword, testAccountEmail);
            userManager.createUser(testJournalist);

            return true;
        }

        return false;
    }


}
