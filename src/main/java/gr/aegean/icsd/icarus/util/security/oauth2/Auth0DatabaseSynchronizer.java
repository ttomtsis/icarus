package gr.aegean.icsd.icarus.util.security.oauth2;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Response;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.icarususer.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.exceptions.async.AsyncExecutionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class Auth0DatabaseSynchronizer {


    private static final Logger log = LoggerFactory.getLogger(Auth0DatabaseSynchronizer.class);


    private final IcarusUserRepository icarusUserRepository;
    private final OAuth2Configuration configuration;



    public Auth0DatabaseSynchronizer(IcarusUserRepository icarusUserRepository,
                                     OAuth2Configuration configuration) {

        this.icarusUserRepository = icarusUserRepository;
        this.configuration = configuration;
    }



    @Bean
    @Async
    @Transactional
    public void synchroniseDatabase() throws Auth0Exception {

        try {
            ManagementAPI management = connectToManagementAPI();

            Set<String> auth0Users = getAuth0Users(management);

            synchroniseDatabase(auth0Users);
        }
        catch (RuntimeException ex) {

            log.error("Failed to synchronize the local database with Auth0");
            throw new AsyncExecutionFailedException(ex);
        }

    }


    private ManagementAPI connectToManagementAPI() throws Auth0Exception {

        log.warn("Connecting to Auth0 Management API");

        // Auth0 domain shared across auth0 apps
        String domain = configuration.getDomain();

        // ID and Secret of the M2M Auth0 app
        String id = configuration.getClientId();
        String secret = configuration.getClientSecret();

        // Identifier of the auth0 management api
        String managementApiId = configuration.getManagementApiId();

        AuthAPI auth = AuthAPI.newBuilder(domain, id, secret).build();
        Response<TokenHolder> response = auth.requestToken(managementApiId).execute();
        TokenHolder holder = response.getBody();
        String managementApiToken = holder.getAccessToken();

        log.warn("Connection to Auth0 Management API successful");

        return ManagementAPI.newBuilder(domain, managementApiToken).build();
    }


    private Set<String> getAuth0Users(ManagementAPI management) throws Auth0Exception {

        log.warn("Querying all registered users from Auth0");

        int page = 0;
        int perPage = 50;
        boolean includeTotals = true;

        Set<String> usernames = new HashSet<>();
        UsersPage users;

        do {
            UserFilter userFilter = new UserFilter().withPage(page, perPage).withTotals(includeTotals);
            users = management.users().list(userFilter).execute().getBody();

            page++;

            for (User user : users.getItems()) {
                usernames.add(user.getUsername());
            }

        } while (users.getStart() + users.getLength() < users.getTotal());

        log.warn("Querying Auth0 registered users successful");

        return usernames;
    }


    private void synchroniseDatabase(Set<String> auth0Usernames) {

        log.warn("Synchronizing local database with Auth0, deleting local users that no longer exist");

        List<IcarusUser> localUsers = icarusUserRepository.findAll();

        for (IcarusUser localUser : localUsers) {

            String localUsersUsername = localUser.getUsername();

            // Auth0 usernames are in lowercase
            if (!auth0Usernames.contains(localUsersUsername.toLowerCase())) {
                icarusUserRepository.deleteIcarusUserByUsername(localUsersUsername);
                log.warn("Deleting local user: {}", localUsersUsername);
            }
        }

        log.warn("Database synchronization complete");
    }


}
