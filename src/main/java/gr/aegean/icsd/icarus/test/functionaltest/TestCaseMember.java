package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import jakarta.persistence.*;
import org.springframework.http.HttpStatusCode;


@Entity
public class TestCaseMember {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int expectedResponseCode;

    private String expectedResponseBody;

    private String requestPathVariable;

    private String requestBody;

    @ManyToOne(targetEntity = FunctionalTest.class, optional = false)
    private PerformanceTest parentTestCase;



    public static class TestCaseMemberBuilder {
        private final int expectedResponseCode;

        private final String expectedResponseBody;

        private String requestPathVariable;

        private String requestBody;

        public TestCaseMemberBuilder(HttpStatusCode expectedResponseCode, String expectedResponseBody) {
            this.expectedResponseCode = expectedResponseCode.value();
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

        this.requestPathVariable = builder.requestPathVariable;
        this.requestBody = builder.requestBody;
    }

    public TestCaseMember() {}



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

    public String getRequestPathVariable() {
        return requestPathVariable;
    }

    public void setRequestPathVariable(String requestPathVariable) {
        this.requestPathVariable = requestPathVariable;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public PerformanceTest getParentTestCase() {
        return parentTestCase;
    }


}
