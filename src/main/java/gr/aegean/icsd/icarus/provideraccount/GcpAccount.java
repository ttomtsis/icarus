package gr.aegean.icsd.icarus.provideraccount;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class GcpAccount extends ProviderAccount {

    @NotBlank(message = "GCP Account Keyfile cannot be blank")
    @JdbcTypeCode(SqlTypes.JSON)
    private String gcpKeyfile;

    @NotBlank(message = "GCP Account project id cannot be blank")
    private String gcpProjectId;


    public GcpAccount(String keyfile, String project, String name, String description) {
        super(name,description);
        this.gcpKeyfile = keyfile;
        this.gcpProjectId = project;
    }

    public GcpAccount(String keyfile, String project, String name) {
        super(name);
        this.gcpProjectId = project;
        this.gcpKeyfile = keyfile;
    }

    public GcpAccount() {}

    public static GcpAccount createAccountFromModel(ProviderAccountModel gcpAccountModel) {
        String name = gcpAccountModel.getName();
        String description = gcpAccountModel.getDescription();
        String gcpKeyfile = gcpAccountModel.getGcpKeyfile();
        String gcpProject = gcpAccountModel.getGcpProjectId();

        if (StringUtils.isBlank(description)) {
            return new GcpAccount(gcpKeyfile, gcpProject, name);
        }

        return new GcpAccount(gcpKeyfile, gcpProject, name, description);
    }


    public String getGcpKeyfile() {
        return this.gcpKeyfile;
    }

    public void setGcpKeyfile(String newKeyfile) {
        this.gcpKeyfile = newKeyfile;
    }

    public String getGcpProjectId() {
        return gcpProjectId;
    }

    public void setGcpProjectId(String gcpProjectId) {
        this.gcpProjectId = gcpProjectId;
    }


}
