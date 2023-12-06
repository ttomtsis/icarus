package gr.aegean.icsd.icarus.util.configuration.security;

import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.configuration.security.httpbasic.MySqlAuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import static jakarta.servlet.DispatcherType.*;


/**
 * Configuration class defining the necessary options for application wide security configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {


    @Value("${users.enableTestUser}")
    private boolean enableTestAccounts;

    @Value("${users.testUserUsername}")
    private String testAccountUsername;

    @Value("${users.testUserPassword}")
    private String testAccountPassword;

    @Value("${users.testUserEmail}")
    private String testAccountEmail;


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
    SecurityFilterChain createFilterChain(HttpSecurity http) throws Exception {

        http

                .authorizeHttpRequests((authorize) -> authorize

                        .dispatcherTypeMatchers(FORWARD, ERROR, INCLUDE).permitAll()

                        // Authentication endpoints
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers("/").authenticated()

                        .requestMatchers("/api/v0/users/{username}/accounts/**").authenticated()
                        .requestMatchers("/api/v0/tests/**").authenticated()

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

                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .csrf(AbstractHttpConfigurer::disable)

                .httpBasic(Customizer.withDefaults());

        return http.build();

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
    boolean createTestUsers(MySqlAuthenticationManager userManager) {

        if (enableTestAccounts && !userManager.userExists(testAccountUsername)) {

            DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
            passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

            String password = passwordEncoder.encode(testAccountPassword);

            UserDetails testJournalist = new IcarusUser(testAccountUsername, password, testAccountEmail);
            userManager.createUser(testJournalist);

            return true;
        }

        return false;

    }


}
