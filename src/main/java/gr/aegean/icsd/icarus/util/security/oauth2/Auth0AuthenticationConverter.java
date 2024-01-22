package gr.aegean.icsd.icarus.util.security.oauth2;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;

import static gr.aegean.icsd.icarus.util.security.oauth2.OAuth2Configuration.AUDIENCE;


public class Auth0AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {

        String id = (String) source.getClaims().get(AUDIENCE + "/id");
        String username = (String) source.getClaims().get(AUDIENCE + "/username");
        String email = (String) source.getClaims().get(AUDIENCE + "/email");

        IcarusUser icarusUser = new IcarusUser();

        icarusUser.setId(id);
        icarusUser.setUsername(username);
        icarusUser.setEmail(email);

        return new UsernamePasswordAuthenticationToken(icarusUser, "N/A", Collections.emptyList());
    }


}
