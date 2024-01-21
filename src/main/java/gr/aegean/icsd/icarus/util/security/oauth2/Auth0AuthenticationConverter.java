package gr.aegean.icsd.icarus.util.security.oauth2;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;


public class Auth0AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {

        // Extract the custom claims from the JWT
        String id = (String) source.getClaims().get("https://www.icarus.com/id");
        String username = (String) source.getClaims().get("https://www.icarus.com/username");
        String email = (String) source.getClaims().get("https://www.icarus.com/email");


        // Create an instance of IcarusUser
        IcarusUser icarusUser = new IcarusUser();

        icarusUser.setUsername(username);
        icarusUser.setEmail(email);

        return new UsernamePasswordAuthenticationToken(icarusUser, "N/A", Collections.emptyList());
    }


}
