package gr.aegean.icsd.icarus.util.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OAuth2Configuration {


    public static String AUDIENCE;


    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    public void setAUDIENCE(String audience) {
        OAuth2Configuration.AUDIENCE = audience;
    }


}
