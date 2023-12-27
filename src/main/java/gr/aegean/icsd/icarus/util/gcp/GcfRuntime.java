package gr.aegean.icsd.icarus.util.gcp;

/**
 * Contains a list of all supported Google Cloud Function Runtimes <br>
 */
public enum GcfRuntime {

    // Node.js Runtimes
    NODEJS20("nodejs20"),
    NODEJS18("nodejs18"),
    NODEJS16("nodejs16"),
    NODEJS14("nodejs14"),
    NODEJS12("nodejs12"),
    NODEJS10("nodejs10"),
    NODEJS8("nodejs8"),
    NODEJS6("nodejs6"),

    // Python Runtimes
    PYTHON312("python312"),
    PYTHON311("python311"),
    PYTHON310("python310"),
    PYTHON39("python39"),
    PYTHON38("python38"),
    PYTHON37("python37"),

    // Go Runtimes
    GO121("go121"),
    GO120("go120"),
    GO119("go119"),
    GO118("go118"),
    GO116("go116"),
    GO113("go113"),
    GO112("go112"),
    GO111("go111"),

    // Java Runtimes
    JAVA17("java17"),
    JAVA11("java11"),

    // Ruby Runtimes
    RUBY32("ruby32"),
    RUBY30("ruby30"),
    RUBY27("ruby27"),
    RUBY26("ruby26"),

    // PHP Runtimes
    PHP82("php82"),
    PHP81("php81"),
    PHP74("php74"),

    // .NET Runtimes
    DOTNET6("dotnet6"),
    DOTNET3("dotnet3");



    private final String gcfRuntimeCode;

    GcfRuntime(String gcfRuntimeCode) {
        this.gcfRuntimeCode = gcfRuntimeCode;
    }

    public String get() {
        return gcfRuntimeCode;
    }

}
