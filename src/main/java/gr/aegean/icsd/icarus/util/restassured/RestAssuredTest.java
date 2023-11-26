package gr.aegean.icsd.icarus.util.restassured;

import gr.aegean.icsd.icarus.util.enums.Platform;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;


/**
 * Represents a simple functional Rest Assured test <br>
 *
 * Evaluates the output of a function based on the desired input
 */
public class RestAssuredTest {

    private final Method httpMethod;
    private final Platform provider;

    private final String path;
    private final String pathVariable;
    private final String requestPathVariableValue;
    private final String requestBody;

    private final int expectedStatusCode;
    private final String expectedResponseBody;

    private int actualStatusCode;
    private String actualResponseBody;

    private boolean pass;



    /**
     * Default constructor, instantiates the test with expected inputs
     *
     * @param functionURL Invocation URL of the function
     * @param path Path the function exposes
     * @param pathVariable Path variable present in the path
     * @param pathVariableValue Value of the path variable
     * @param requestBody Body of the outbound request
     * @param expectedStatusCode Expected status code from the response
     * @param expectedResponseBody Expected response body from the response
     * @param provider Cloud provider where the function is deployed
     */
    public RestAssuredTest(String functionURL, String path, String pathVariable,
                           String pathVariableValue, String requestBody,
                           int expectedStatusCode, String expectedResponseBody, Platform provider) {

        this.path = path;
        this.pathVariable = pathVariable;

        this.requestPathVariableValue = pathVariableValue;
        this.requestBody = requestBody;

        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponseBody = expectedResponseBody;

        RestAssured.baseURI = functionURL;

        this.httpMethod = Method.GET;

        this.provider = provider;

    }



    /**
     * Uses RestAssured to execute the Test Case
     */
    public void runTest() {

        Response response =
                given().
                        pathParam(this.pathVariable, this.requestPathVariableValue).
                        body(requestBody).
                when().
                        request(this.httpMethod, this.path).
                then().
                        extract().
                        response();

        this.actualStatusCode = response.getStatusCode();

        if (this.provider.equals(Platform.AWS) ) {
            this.actualResponseBody = response.jsonPath().getString("message");
        }
        else if (this.provider.equals(Platform.GCP)){
            this.actualResponseBody = response.getBody().asString();
        }

        if (this.actualResponseBody.equals(this.expectedResponseBody) && this.actualStatusCode == this.expectedStatusCode ) {
            this.pass = true;
        }

    }


    /**
     * Returns the actual response body
     * @return Actual Response Body
     */
    public String getActualResponseBody() {
        return this.actualResponseBody;
    }


    /**
     * Returns the actual response code
     * @return Actual Response Code
     */
    public int getActualResponseCode() {
        return this.actualStatusCode;
    }


    /**
     * Returns the result of the test's execution
     * @return Pass/Fail result
     */
    public boolean getPass() {
        return this.pass;
    }


}
