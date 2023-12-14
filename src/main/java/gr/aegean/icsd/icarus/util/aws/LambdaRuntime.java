package gr.aegean.icsd.icarus.util.aws;

/**
 * Contains a list of all supported AWS Lambda Runtimes <br>
 * Custom Runtimes are NOT supported
 */
public enum LambdaRuntime {

    /*
    AWS Lambda Runtime codes contain dots, which are not valid symbols to define enum
    constants, hence as a workaround each Lambda Runtime code is associated with an enum constant.

    The actual lambda runtime code is fetched by using the 'get' method implemented below.

    This approach was chosen because of the type safety offered by Java enums
     */

    // Node.js Runtimes
    nodejs20("nodejs20.x"),
    nodejs18("nodejs18.x"),
    nodejs16("nodejs16.x"),
    nodejs14("nodejs14.x"),

    // Python Runtimes
    python311("python3.11"),
    python310("python3.10"),
    python39("python3.9"),
    python38("python3.8"),
    python37("python3.7"),

    // Java Runtimes
    java21("java21"),
    java17("java17"),
    java11("java11"),
    java8al2("java8al2"),
    java8("java6"),

    // .NET Runtimes
    net7("dotnet7"),
    net6("dotnet6"),

    // Go Runtimes
    go1("go1.x"),

    // Ruby Runtimes
    ruby32("ruby3.2"),
    ruby27("ruby2.7");

    private final String lambdaRuntimeCode;

    LambdaRuntime(String runtimeCode) { this.lambdaRuntimeCode = runtimeCode;}

    /**
     * Used to get the actual lambdaRuntimeCode that will be used by Terraform
     * @return AWS Lambda Runtime code
     */
    public String get() {
        return lambdaRuntimeCode;
    }

}
