package gr.aegean.icsd.icarus.util.security.httpbasic;

import gr.aegean.icsd.icarus.icarususer.IcarusUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


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
     * {@link SqlAuthenticationManager} and {@link BCryptPasswordEncoder}
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    DaoAuthenticationProvider createAuthenticationProvider() {

        DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        SqlAuthenticationManager users = new SqlAuthenticationManager(userRepository, passwordEncoder);

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(users);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }


}
