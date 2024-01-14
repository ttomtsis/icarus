package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;


public class TestModel extends RepresentationModel<TestModel> {


    private Long id;
    private String creator;
    private String name;
    private String description;
    private RequestMethod httpMethod;
    private String path;
    private String pathVariable;
    private Long testAuthor;
    private Long targetFunction;
    private  Set<Long> accountsList;
    private Set<Long> resourceConfigurations;
    private String authorUsername;
    private ExecutionState state;



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
        this.resourceConfigurations = model.getResourceConfigurations();
        this.authorUsername = model.getAuthorUsername();
        this.state = model.getState();
        this.creator = model.getCreator();
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

    public RequestMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(RequestMethod httpMethod) {
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

    public ExecutionState getState() {
        return state;
    }

    public void setState(ExecutionState state) {
        this.state = state;
    }

    public Set<Long> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public void setResourceConfigurations(Set<Long> resourceConfigurations) {
        this.resourceConfigurations = resourceConfigurations;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


}
