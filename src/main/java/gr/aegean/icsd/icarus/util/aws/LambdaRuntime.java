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
    NODEJS20("nodejs20.x"),
    NODEJS18("nodejs18.x"),
    NODEJS16("nodejs16.x"),
    NODEJS14("nodejs14.x"),

    // Python Runtimes
    PYTHON311("python3.11"),
    PYTHON310("python3.10"),
    PYTHON39("python3.9"),
    PYTHON38("python3.8"),
    PYTHON37("python3.7"),

    // Java Runtimes
    JAVA21("java21"),
    JAVA17("java17"),
    JAVA11("java11"),
    JAVA8AL2("java8al2"),
    JAVA8("java6"),

    // .NET Runtimes
    NET7("dotnet7"),
    NET6("dotnet6"),

    // Go Runtimes
    GO1("go1.x"),

    // Ruby Runtimes
    RUBY32("ruby3.2"),
    RUBY27("ruby2.7");


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
