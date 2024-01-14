package gr.aegean.icsd.icarus.util.security;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Utility class querying information about the authenticated user
 */
public final class UserUtils {


    private UserUtils() {}



    public static String getUsername() {

        return SecurityContextHolder.getContext().getAuthentication().getName();

    }


    public static IcarusUser getLoggedInUser() {

        return (IcarusUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
