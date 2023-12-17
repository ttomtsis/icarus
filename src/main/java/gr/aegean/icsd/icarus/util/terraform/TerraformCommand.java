package gr.aegean.icsd.icarus.util.terraform;

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
        return command;
    }


}

