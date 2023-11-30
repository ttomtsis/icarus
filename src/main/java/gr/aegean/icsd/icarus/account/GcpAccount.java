package gr.aegean.icsd.icarus.account;

import bsh.StringUtil;
import gr.aegean.icsd.icarus.util.exceptions.GcpCredentialsNotFoundException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Entity
public class GcpAccount extends ProviderAccount {

    @NotBlank(message = "Keyfile cannot be blank")
    @JdbcTypeCode(SqlTypes.JSON)
    private String gcpKeyfile;


    public GcpAccount(File keyfile, String name, String description) {
        super(name,description);
        this.gcpKeyfile = parseJSON(keyfile);
    }

    public GcpAccount(String keyfile, String name, String description) {
        super(name,description);
        this.gcpKeyfile = keyfile;
    }

    public GcpAccount(File keyfile, String name) {
        super(name);
        this.gcpKeyfile = parseJSON(keyfile);
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


    public String parseJSON(File keyfile) {

        try {
            byte[] bytes = Files.readAllBytes(keyfile.toPath());
            return new String(bytes);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        return null;

    }

    public String getGcpKeyfile() {
        return this.gcpKeyfile;
    }

    public void setGcpKeyfile(String newKeyfile) {
        this.gcpKeyfile = newKeyfile;
    }

    public void updateGcpKeyFile(File newKeyfile) {
        this.gcpKeyfile = parseJSON(newKeyfile);
    }

}
