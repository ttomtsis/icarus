package gr.aegean.icsd.icarus.util.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "security.auth0")
public class OAuth2Configuration {


    private String clientId;
    private String clientSecret;
    private String domain;
    private String managementApiId;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private String audiences;



    public String getAudiences() {
        return audiences;
    }

    public void setAudiences(String audiences) {
        this.audiences = audiences;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getManagementApiId() {
        return managementApiId;
    }

    public void setManagementApiId(String managementApiId) {
        this.managementApiId = managementApiId;
    }


}
