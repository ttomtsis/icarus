package gr.aegean.icsd.icarus.account;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

@Entity
public class AwsAccount extends ProviderAccount{

    @NotBlank(message = "Access Key cannot be blank")
    private String awsAccessKey;

    @NotBlank(message = "Secret Key cannot be blank")
    private String awsSecretKey;


    public AwsAccount(String accessKey, String secretKey, String name, String description) {
        super(name,description);
        this.awsAccessKey = accessKey;
        this.awsSecretKey = secretKey;
    }

    public AwsAccount(String accessKey, String secretKey, String name) {
        super(name);
        this.awsAccessKey = accessKey;
        this.awsSecretKey = secretKey;
    }

    public AwsAccount() {}


    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }
}
