package gr.aegean.icsd.icarus.util.configuration.security;

import gr.aegean.icsd.icarus.user.IcarusUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;


/**
 * Contains configuration options for setting up HTTP Basic Authentication
 */
@Configuration
public class HttpBasicConfiguration {

    private final IcarusUserRepository userRepository;


    public HttpBasicConfiguration (IcarusUserRepository icarusUserRepository) {
        this.userRepository = icarusUserRepository;
    }


    /**
     * Creates a DaoAuthenticationProvider that utilizes the
     * {@link MySqlAuthenticationManager} and {@link BCryptPasswordEncoder}
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    DaoAuthenticationProvider createAuthenticationProvider() {

        MySqlAuthenticationManager users = new MySqlAuthenticationManager(userRepository);

        DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(users);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;

    }


}
