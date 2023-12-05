package gr.aegean.icsd.icarus.account;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class GcpAccount extends ProviderAccount {

    @NotBlank(message = "Keyfile cannot be blank")
    @JdbcTypeCode(SqlTypes.JSON)
    private String gcpKeyfile;


    public GcpAccount(String keyfile, String name, String description) {
        super(name,description);
        this.gcpKeyfile = keyfile;
    }

    public GcpAccount(String keyfile, String name) {
        super(name);
        this.gcpKeyfile = keyfile;
    }

    public GcpAccount() {}

    public static GcpAccount createAccountFromModel(ProviderAccountModel gcpAccountModel) {
        String name = gcpAccountModel.getName();
        String description = gcpAccountModel.getDescription();
        String gcpKeyfile = gcpAccountModel.getGcpKeyfile();

        if (StringUtils.isBlank(description)) {
            return new GcpAccount(gcpKeyfile, name);
        }

        return new GcpAccount(gcpKeyfile, name, description);
    }


    public String getGcpKeyfile() {
        return this.gcpKeyfile;
    }

    public void setGcpKeyfile(String newKeyfile) {
        this.gcpKeyfile = newKeyfile;
    }


}
