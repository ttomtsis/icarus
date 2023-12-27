package gr.aegean.icsd.icarus.test.functionaltest.testcasemember;

import gr.aegean.icsd.icarus.test.functionaltest.testcase.TestCase;
import jakarta.persistence.*;


@Entity
public class TestCaseMember {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int expectedResponseCode;

    private String expectedResponseBody;

    private String requestPathVariableValue;

    private String requestBody;

    @ManyToOne(targetEntity = TestCase.class, optional = false)
    private TestCase parentTestCase;



    public static class TestCaseMemberBuilder {
        private final int expectedResponseCode;

        private final String expectedResponseBody;

        private String requestPathVariable;

        private String requestBody;

        public TestCaseMemberBuilder(int expectedResponseCode, String expectedResponseBody) {
            this.expectedResponseCode = expectedResponseCode;
            this.expectedResponseBody = expectedResponseBody;
        }

        public TestCaseMemberBuilder requestPathVariable(String requestPathVariable) {
            this.requestPathVariable = requestPathVariable;
            return this;
        }

        public TestCaseMemberBuilder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public TestCaseMember build() {
            return new TestCaseMember(this);
        }


    }



    public TestCaseMember(TestCaseMemberBuilder builder) {
        this.expectedResponseCode = builder.expectedResponseCode;
        this.expectedResponseBody = builder.expectedResponseBody;

        this.requestPathVariableValue = builder.requestPathVariable;
        this.requestBody = builder.requestBody;
    }

    public TestCaseMember() {}

    public static TestCaseMember createTestCaseMemberFromModel(TestCaseMemberModel model) {

        return new TestCaseMemberBuilder(model.getExpectedResponseCode(),
                model.getExpectedResponseBody())

                .requestPathVariable(model.getRequestPathVariableValue())
                .requestBody(model.getRequestBody())
                .build();
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getExpectedResponseCode() {
        return expectedResponseCode;
    }

    public void setExpectedResponseCode(int expectedResponseCode) {
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

    public TestCase getParentTestCase() {
        return parentTestCase;
    }

    public void setParentTestCase(TestCase parentTestCase) {
        this.parentTestCase = parentTestCase;
    }


}
