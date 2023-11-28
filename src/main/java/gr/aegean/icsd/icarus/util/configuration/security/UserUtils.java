package gr.aegean.icsd.icarus.util.configuration.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class UserUtils {


    public static String getUsername() {

        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

    }


}
