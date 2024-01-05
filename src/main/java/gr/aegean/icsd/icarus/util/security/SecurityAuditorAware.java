package gr.aegean.icsd.icarus.util.security;


import gr.aegean.icsd.icarus.user.IcarusUser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class SecurityAuditorAware implements AuditorAware<IcarusUser> {

    @Override
    public @NotNull Optional<IcarusUser> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {

            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof IcarusUser icarusUser) {

            return Optional.of(icarusUser);

        } else {

            LoggerFactory.getLogger("Security Auditor Aware").error("Principal is not an instance of IcarusUser");
            return Optional.empty();
        }

    }
}


