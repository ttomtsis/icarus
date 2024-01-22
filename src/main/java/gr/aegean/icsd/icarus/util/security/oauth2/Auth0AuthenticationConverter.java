package gr.aegean.icsd.icarus.util.security.oauth2;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class Auth0AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    private final OAuth2Configuration configuration;



    public Auth0AuthenticationConverter(OAuth2Configuration configuration) {
        this.configuration = configuration;
    }



    @Override
    public AbstractAuthenticationToken convert(Jwt source) {

        String audience = configuration.getAudiences();
        String id = (String) source.getClaims().get(audience + "/id");
        String username = (String) source.getClaims().get(audience + "/username");
        String email = (String) source.getClaims().get(audience + "/email");

        IcarusUser icarusUser = new IcarusUser();

        icarusUser.setId(id);
        icarusUser.setUsername(username);
        icarusUser.setEmail(email);

        return new UsernamePasswordAuthenticationToken(icarusUser, "N/A", Collections.emptyList());
    }


}
