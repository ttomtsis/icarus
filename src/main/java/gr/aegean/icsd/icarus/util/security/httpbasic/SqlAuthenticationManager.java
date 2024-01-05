package gr.aegean.icsd.icarus.util.security.httpbasic;

import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.user.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;

import static gr.aegean.icsd.icarus.util.security.SecurityConfiguration.CREDENTIALS_EXPIRATION_PERIOD;


/**
 * Authentication Manager that utilizes {@link IcarusUserRepository} to query
 * user details from a MySQL database
 */
@Service
public class SqlAuthenticationManager implements UserDetailsService, UserDetailsManager {


    private final IcarusUserRepository userRepository;
    private final DelegatingPasswordEncoder passwordEncoder;



    public SqlAuthenticationManager(IcarusUserRepository userRepository,
                                    DelegatingPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public void createUser(UserDetails user) {

        if (user instanceof IcarusUser newUser) {

            String encodedPassword = passwordEncoder.encode(newUser.getPassword());
            newUser.setPassword(encodedPassword);

            userRepository.save(newUser);
        }
        else {
            String notValidIcarusUserExceptionMessage = "The given user is not a valid IcarusUser Entity";
            throw new IllegalArgumentException(notValidIcarusUserExceptionMessage);
        }

    }

    public IcarusUser createIcarusUser(IcarusUser newUser) {

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public void updateUser(UserDetails user) {

        if (user instanceof  IcarusUser updatedUserDetails) {

            IcarusUser loggedInUser = UserUtils.getLoggedInUser();

            setIfNotNull(loggedInUser::setUsername, updatedUserDetails.getUsername());
            setIfNotNull(loggedInUser::setEmail, updatedUserDetails.getEmail());

            if (updatedUserDetails.getPassword() != null) {

                loggedInUser.setPassword(passwordEncoder.encode(updatedUserDetails.getPassword()));
                loggedInUser.setCredentialsLastChanged(Instant.now());
            }

            userRepository.save(loggedInUser);
        }

        throw new IllegalArgumentException("The given user is not a valid IcarusUser Entity");
    }

    @Override
    public void deleteUser(String username) {

        userRepository.deleteIcarusUserByUsername(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("The change password operation has not yet been implemented");
    }

    @Override
    public boolean userExists(String username) {

        return userRepository.findUserByUsername(username).isPresent();
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<IcarusUser> requestedUser = userRepository.findUserByUsername(username);

        if (requestedUser.isPresent()) {

            credentialsNotExpired(requestedUser.get());

            return requestedUser.get();
        }

        else {

            throw new UsernameNotFoundException("User with username: " + username + " was not found");
        }

    }

    private void credentialsNotExpired(IcarusUser user) {

        Instant credentialsLastChanged = user.getCredentialsLastChanged().truncatedTo(ChronoUnit.DAYS);

        Instant expirationDate = credentialsLastChanged.plus(CREDENTIALS_EXPIRATION_PERIOD, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS);

        Instant currentDate = Instant.now().truncatedTo(ChronoUnit.DAYS);

        if (currentDate.isAfter(expirationDate)){
            LoggerFactory.getLogger("SQL Authentication Manager").error("User's credentials have expired");
            throw new CredentialsExpiredException("User's credentials have expired");
        }

    }


    private void setIfNotNull(Consumer<String> setter, String value) {

        if (value != null) {
            setter.accept(value);
        }
    }


}
