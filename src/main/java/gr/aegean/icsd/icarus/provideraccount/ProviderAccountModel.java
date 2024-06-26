package gr.aegean.icsd.icarus.provideraccount;

import org.springframework.hateoas.RepresentationModel;


public class ProviderAccountModel extends RepresentationModel<ProviderAccountModel> {


    private Long id;
    private String creator;
    private String name;
    private String description;

    private String awsAccessKey;
    private String awsSecretKey;

    private String gcpKeyfile;
    private String gcpProjectId;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public String getGcpKeyfile() {
      return gcpKeyfile;
    }

    public void setGcpKeyfile(String gcpKeyfile) {
        this.gcpKeyfile = gcpKeyfile;
    }

    public String getGcpProjectId() {
        return gcpProjectId;
    }

    public void setGcpProjectId(String gcpProjectId) {
        this.gcpProjectId = gcpProjectId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


}
