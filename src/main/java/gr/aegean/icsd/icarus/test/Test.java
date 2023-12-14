package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.TestState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "test")
public class Test {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Test name cannot be blank")
    @Size(min = minLength, max = maxLength, message = "Test name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = minLength, max = maxDescriptionLength,
            message = "Test description does not conform to length limitations")
    private String description;

    @NotBlank(message = "Http Method used by the Test cannot be blank")
    private String httpMethod;

    @Pattern(regexp = "^/([a-zA-Z]+/?)*(\\{[a-zA-Z]+\\}/([a-zA-Z]+/?)*)?$",
            message = "The exposed path is not in a valid format")
    private String path;

    @Pattern(regexp = "\\{(?!\\d*\\})[a-zA-Z0-9_]+\\}",
            message = "Path variable exposed in the test is not in a valid format." +
                    " Path variable must be in the format {A-Z, a-z, 0-9} e.g. {variable1}")
    private String pathVariable;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "test_author_id")
    private IcarusUser testAuthor;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "target_function_id")
    private Function targetFunction;

    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = ProviderAccount.class)
    private final Set<ProviderAccount> accountsList = new HashSet<>();

    // TODO: Merge this field with testAuthor field
    @CreatedBy
    private String authorUsername;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestState state;



    @PreRemove
    private void removeForeignKeyConstraints() {
        this.targetFunction = null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestState getState() {
        return state;
    }

    public void setState(TestState state) {
        this.state = state;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setTestAuthor(IcarusUser testAuthor) {
        this.testAuthor = testAuthor;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IcarusUser getTestAuthor() {return this.testAuthor;}

    public Set<ProviderAccount> getAccountsList() {
        return accountsList;
    }

    public void addProviderAccount(ProviderAccount newAccount) {accountsList.add(newAccount);}

    public String getPathVariable() {
        return pathVariable;
    }

    public void setPathVariable(String pathVariable) {
        this.pathVariable = pathVariable;
    }

    public Function getTargetFunction() {
        return targetFunction;
    }

    protected void setAuthor(IcarusUser testAuthor) {
        this.testAuthor = testAuthor;
    }

    protected void setTargetFunction(Function targetFunction) {
        this.targetFunction = targetFunction;
    }


}
