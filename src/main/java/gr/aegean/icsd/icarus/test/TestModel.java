package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.util.enums.TestState;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;


public class TestModel extends RepresentationModel<TestModel> {

    private Long id;
    private String name;
    private String description;
    private String httpMethod;
    private String path;
    private String pathVariable;
    private Long testAuthor;
    private Long targetFunction;
    private  Set<Long> accountsList;
    private String authorUsername;
    private TestState state;



    public TestModel(Long id, String name, String description, String httpMethod,
                     String path, String pathVariable, Long testAuthor,
                     Long targetFunction, Set<Long> accountsList, String authorUsername,
                     TestState state) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.httpMethod = httpMethod;
        this.path = path;
        this.pathVariable = pathVariable;
        this.testAuthor = testAuthor;
        this.targetFunction = targetFunction;
        this.accountsList = accountsList;
        this.authorUsername = authorUsername;
        this.state = state;
    }

    public TestModel(TestModel model) {

        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.httpMethod = model.getHttpMethod();
        this.path = model.getPath();
        this.pathVariable = model.getPathVariable();
        this.testAuthor = model.getTestAuthor();
        this.targetFunction = model.getTestAuthor();
        this.accountsList = model.getAccountsList();
        this.authorUsername = model.getAuthorUsername();
        this.state = model.getState();
    }

    public TestModel() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathVariable() {
        return pathVariable;
    }

    public void setPathVariable(String pathVariable) {
        this.pathVariable = pathVariable;
    }

    public Long getTestAuthor() {
        return testAuthor;
    }

    public void setTestAuthor(Long testAuthor) {
        this.testAuthor = testAuthor;
    }

    public Long getTargetFunction() {
        return targetFunction;
    }

    public void setTargetFunction(Long targetFunction) {
        this.targetFunction = targetFunction;
    }

    public Set<Long> getAccountsList() {
        return accountsList;
    }

    public void setAccountsList(Set<Long> accountsList) {
        this.accountsList = accountsList;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public TestState getState() {
        return state;
    }

    public void setState(TestState state) {
        this.state = state;
    }


}
