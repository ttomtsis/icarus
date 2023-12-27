package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.enums.TestState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Size(min = MIN_LENGTH, max = MAX_LENGTH,
            message = "Test name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Test description does not conform to length limitations")
    private String description;

    @NotBlank(message = "Http Method used by the Test cannot be blank")
    private String httpMethod;

    // TODO: Add validation for path and pathVariable
    private String path;

    private String pathVariable;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "test_author_id")
    private IcarusUser testAuthor;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "target_function_id")
    private Function targetFunction;

    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = ProviderAccount.class,
    fetch = FetchType.EAGER)
    private final Set<ProviderAccount> accountsList = new HashSet<>();

    @OneToMany(mappedBy = "parentTest", cascade = {CascadeType.REFRESH, CascadeType.REMOVE},
            targetEntity = ResourceConfiguration.class, orphanRemoval = true,
    fetch = FetchType.EAGER)
    private final Set<ResourceConfiguration> resourceConfigurations = new HashSet<>();

    // TODO: Merge this field with testAuthor field
    @CreatedBy
    private String authorUsername;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestState state;




    public Test() {this.state = TestState.CREATED;}




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

    public String getAuthorUsername() {
        return authorUsername;
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

    public void clearAccountsList() {
        accountsList.clear();
    }

    public void removeAccount(ProviderAccount providerAccount) {
        this.accountsList.remove(providerAccount);
    }

    public String getPathVariable() {
        return pathVariable;
    }

    public void setPathVariable(String pathVariable) {
        this.pathVariable = pathVariable;
    }

    public Function getTargetFunction() {
        return targetFunction;
    }

    public void setAuthor(IcarusUser testAuthor) {
        this.testAuthor = testAuthor;
    }

    public void setTargetFunction(Function targetFunction) {
        this.targetFunction = targetFunction;
    }

    public Set<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }


}
