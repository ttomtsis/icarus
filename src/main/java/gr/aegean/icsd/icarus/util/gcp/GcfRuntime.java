package gr.aegean.icsd.icarus.util.gcp;

/**
 * Contains a list of all supported Google Cloud Function Runtimes <br>
 */
public enum GcfRuntime {

    // Node.js Runtimes
    nodejs20,
    nodejs18,
    nodejs16,
    nodejs14,
    nodejs12,
    nodejs10,
    nodejs8,
    nodejs6,

    // Python Runtimes
    python312,
    python311,
    python310,
    python39,
    python38,
    python37,

    // Go Runtimes
    go121,
    go120,
    go119,
    go118,
    go116,
    go113,
    go112,
    go111,

    // Java Runtimes
    java17,
    java11,

    // Ruby Runtimes
    ruby32,
    ruby30,
    ruby27,
    ruby26,

    // PHP Runtimes
    php82,
    php81,
    php74,

    // .NET Runtimes
    dotnet6,
    dotnet3

}
