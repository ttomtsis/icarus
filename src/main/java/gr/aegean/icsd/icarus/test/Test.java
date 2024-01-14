package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "test")
@EntityListeners(AuditingEntityListener.class)
public class Test {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @CreatedBy
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "test_author_id", updatable = false)
    private IcarusUser creator;

    @NotBlank(message = "Test name cannot be blank")
    @Size(min = MIN_LENGTH, max = MAX_LENGTH,
            message = "Test name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Test description does not conform to length limitations")
    private String description;

    @NotNull(message = "Http Method used by the Test cannot be blank")
    @Enumerated(EnumType.STRING)
    private RequestMethod httpMethod;

    @Pattern(regexp = "^[-a-zA-Z0-9@:%_+.~#?&=/{}]*$", message = "Test's exposed path is invalid")
    private String path;

    @Pattern(regexp = "^\\w*$", message = "Test's exposed path variable is invalid")
    private String pathVariable;

    @ManyToOne
    @JoinColumn(name = "target_function_id")
    private Function targetFunction;

    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = ProviderAccount.class,
    fetch = FetchType.EAGER)
    private final Set<ProviderAccount> accountsList = new HashSet<>();

    @OneToMany(mappedBy = "parentTest", cascade = {CascadeType.REFRESH, CascadeType.REMOVE},
            targetEntity = ResourceConfiguration.class, orphanRemoval = true,
    fetch = FetchType.EAGER)
    private final Set<ResourceConfiguration> resourceConfigurations = new HashSet<>();

    @OneToMany(targetEntity = TestExecution.class, mappedBy = "parentTest", cascade = {CascadeType.REFRESH,
            CascadeType.REMOVE}, orphanRemoval = true)
    private final Set<TestExecution> testExecutions = new HashSet<>();



    public Test() {}



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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(RequestMethod httpMethod) {
        this.httpMethod = httpMethod;
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

    public IcarusUser getCreator() {return this.creator;}

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
        this.creator = testAuthor;
    }

    public void setTargetFunction(Function targetFunction) {
        this.targetFunction = targetFunction;
    }

    public Set<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public Set<TestExecution> getTestExecutions() {
        return testExecutions;
    }


}
