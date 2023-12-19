package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import org.springframework.hateoas.RepresentationModel;

public class TestCaseMemberModel extends RepresentationModel<TestCaseMemberModel> {


    private Long id;
    private Integer expectedResponseCode;
    private String expectedResponseBody;
    private String requestPathVariableValue;
    private String requestBody;
    private Long parentTestCase;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExpectedResponseCode() {
        return expectedResponseCode;
    }

    public void setExpectedResponseCode(Integer expectedResponseCode) {
        this.expectedResponseCode = expectedResponseCode;
    }

    public String getExpectedResponseBody() {
        return expectedResponseBody;
    }

    public void setExpectedResponseBody(String expectedResponseBody) {
        this.expectedResponseBody = expectedResponseBody;
    }

    public String getRequestPathVariableValue() {
        return requestPathVariableValue;
    }

    public void setRequestPathVariableValue(String requestPathVariableValue) {
        this.requestPathVariableValue = requestPathVariableValue;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Long getParentTestCase() {
        return parentTestCase;
    }

    public void setParentTestCase(Long parentTestCase) {
        this.parentTestCase = parentTestCase;
    }


}
