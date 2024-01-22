package gr.aegean.icsd.icarus.util.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OAuth2Configuration {


    public static String AUDIENCE;
    public static String CLIENT_ID;
    public static String CLIENT_SECRET;
    public static String DOMAIN;
    public static String MANAGEMENT_API_ID;


    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    public void setAudience(String audience) {
        OAuth2Configuration.AUDIENCE = audience;
    }

    @Value("${security.auth0.clientId}")
    public void setClientId(String clientID) {
        OAuth2Configuration.CLIENT_ID = clientID;
    }

    @Value("${security.auth0.clientSecret}")
    public void setClientSecret(String clientSecret) {
        OAuth2Configuration.CLIENT_SECRET = clientSecret;
    }

    @Value("${security.auth0.domain}")
    public void setDomain(String domain) {
        OAuth2Configuration.DOMAIN = domain;
    }

    @Value("${security.auth0.managementApiId}")
    public void setManagementApiId(String managementApiId) {
        OAuth2Configuration.MANAGEMENT_API_ID = managementApiId;
    }


}
