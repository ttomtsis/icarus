package gr.aegean.icsd.icarus.util.restassured;

import gr.aegean.icsd.icarus.util.enums.Platform;
import io.micrometer.common.util.StringUtils;
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
    private String pathVariable;
    private String requestPathVariableValue;
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
     * @param body Body of the outbound request
     * @param expectedStatusCode Expected status code from the response
     * @param expectedResponseBody Expected response body from the response
     * @param provider Cloud provider where the function is deployed
     */
    public RestAssuredTest(String functionURL, String path, String pathVariable,
                           String pathVariableValue, String body,
                           int expectedStatusCode, String expectedResponseBody, Platform provider) {

        RestAssured.baseURI = functionURL;

        this.path = path;

        if (StringUtils.isNotBlank(this.path)) {
            this.pathVariable = pathVariable;
            this.requestPathVariableValue = pathVariableValue;
        }

        if (StringUtils.isBlank(body)) {
            this.requestBody = "";
        } else {
            this.requestBody = body;
        }

        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponseBody = expectedResponseBody;

        this.httpMethod = Method.GET;

        this.provider = provider;

        runTest();
    }



    /**
     * Uses RestAssured to execute the Test Case
     */
    public void runTest() {

        Response response;
        if (StringUtils.isBlank(this.path)) {
            response =
                    given().
                            body(this.requestBody).
                    when().
                            request(this.httpMethod).
                    then().
                            extract().
                            response();
        }
        else {
            response =
                    given().
                            pathParam(this.pathVariable, this.requestPathVariableValue).
                            body(this.requestBody).
                            when().
                            request(this.httpMethod, this.path).
                            then().
                            extract().
                            response();
        }

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


    @Override
    public String toString() {
        return "RestAssuredTest{" +
                "httpMethod=" + httpMethod +
                ", provider=" + provider +
                ", path='" + path + '\'' +
                ", pathVariable='" + pathVariable + '\'' +
                ", requestPathVariableValue='" + requestPathVariableValue + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", expectedStatusCode=" + expectedStatusCode +
                ", expectedResponseBody='" + expectedResponseBody + '\'' +
                ", actualStatusCode=" + actualStatusCode +
                ", actualResponseBody='" + actualResponseBody + '\'' +
                ", pass=" + pass +
                '}';
    }


}
