package gr.aegean.icsd.icarus.util.security.httpbasic;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.icarususer.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.exceptions.IcarusConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;


/**
 * Contains configuration options for setting up HTTP Basic Authentication
 */
@Configuration
public class HttpBasicConfiguration {


    public static int CREDENTIALS_EXPIRATION_PERIOD;
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final int MAXIMUM_PASSWORD_LENGTH = 150;


    @Value("${security.httpBasic.users.enableTestUser}")
    private boolean enableTestAccounts;

    @Value("${security.httpBasic.users.testUserUsername}")
    private String testAccountUsername;

    @Value("${security.httpBasic.users.testUserPassword}")
    private String testAccountPassword;

    @Value("${security.httpBasic.users.testUserEmail}")
    private String testAccountEmail;

    @Value("${security.httpBasic.enableHttpBasic}")
    private boolean enableHttpBasic;


    @Value("${security.httpBasic.credentialsExpirationPeriod}")
    public void setCredentialsExpirationPeriod(Integer credentialsExpirationPeriod) {

        if (credentialsExpirationPeriod != null && credentialsExpirationPeriod > 0) {
            HttpBasicConfiguration.CREDENTIALS_EXPIRATION_PERIOD = credentialsExpirationPeriod;
        }
        else {
            throw new IcarusConfigurationException("Credentials expiration period cannot be: "
                    + credentialsExpirationPeriod);
        }
    }


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


    @Bean
    DelegatingPasswordEncoder createEncoder() {

        DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return passwordEncoder;
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

        if (enableTestAccounts && enableHttpBasic && !userManager.userExists(testAccountUsername)) {

            UserDetails testJournalist = new IcarusUser(testAccountUsername, testAccountPassword, testAccountEmail);
            userManager.createUser(testJournalist);

            return true;
        }

        return false;
    }


}
