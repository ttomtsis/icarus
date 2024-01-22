package gr.aegean.icsd.icarus.util.security.oauth2;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.icarususer.IcarusUserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;


@Component
public class Auth0AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    private final OAuth2Configuration configuration;
    private final IcarusUserRepository icarusUserRepository;



    public Auth0AuthenticationConverter(OAuth2Configuration configuration,
                                        IcarusUserRepository icarusUserRepository) {
        this.configuration = configuration;
        this.icarusUserRepository = icarusUserRepository;
    }



    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt source) {

        String audience = configuration.getAudiences();
        String id = (String) source.getClaims().get(audience + "/id");
        String username = (String) source.getClaims().get(audience + "/username");
        String email = (String) source.getClaims().get(audience + "/email");

        IcarusUser icarusUser = new IcarusUser();

        icarusUser.setId(id);
        icarusUser.setUsername(username);
        icarusUser.setEmail(email);
        icarusUser.setPassword("OAuth2 used 1 !");

        Optional<IcarusUser> persistedUser = icarusUserRepository.findUserByUsername(username);
        if (persistedUser.isEmpty()) {
            LoggerFactory.getLogger(Auth0AuthenticationConverter.class).warn("Creating local user: {}", username);
            icarusUserRepository.save(icarusUser);
        }
        else {
            // In case a local user migrated to using auth0 authentication.
            // i.e. user registers an account locally then registers an account to auth0 and starts using oauth2
            // to authenticate. In that case the user will continue to use the id he first registered locally
            icarusUser.setId(persistedUser.get().getId());
        }

        return new UsernamePasswordAuthenticationToken(icarusUser, "N/A", Collections.emptyList());
    }


}
