package gr.aegean.icsd.icarus.util.security;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Utility class querying information about the authenticated user
 */
public final class UserUtils {


    private static final Logger log = LoggerFactory.getLogger(UserUtils.class);



    private UserUtils() {}



    public static String getUsername() {

        return SecurityContextHolder.getContext().getAuthentication().getName();

    }


    public static IcarusUser getLoggedInUser() {

        Object loggedInUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedInUser == null) {
            log.error("No user is currently logged in");
            throw new BadCredentialsException("No user is currently logged in");
        }

        if (loggedInUser instanceof IcarusUser icarusUser) {
            return icarusUser;
        }

        log.error("User is not an instance of Icarus User");
        throw new BadCredentialsException("User is not an instance of Icarus User");
    }


}
