package gr.aegean.icsd.icarus.util.terraform;

import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.LOCAL_PROVIDERS_DIRECTORY;
import static gr.aegean.icsd.icarus.util.terraform.TerraformConfiguration.USE_LOCAL_PROVIDERS;

public enum TerraformCommand {


    INIT("terraform", "init"),
    APPLY("terraform", "apply", "-auto-approve"),
    DESTROY("terraform", "destroy", "-auto-approve"),
    OUTPUT("terraform", "output");


    private final String[] command;



    TerraformCommand(String... command) {
        this.command = command;
    }


    public String[] get() {

        if (this == INIT && USE_LOCAL_PROVIDERS) {
            return new String[]{"terraform", "init", "-plugin-dir=" + LOCAL_PROVIDERS_DIRECTORY};
        }

        return command;
    }


}

