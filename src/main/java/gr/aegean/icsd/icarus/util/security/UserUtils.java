package gr.aegean.icsd.icarus.util.security;

import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Utility class querying information about the authenticated user
 */
public final class UserUtils {


    public static String getUsername() {

        return SecurityContextHolder.getContext().getAuthentication().getName();

    }


}
