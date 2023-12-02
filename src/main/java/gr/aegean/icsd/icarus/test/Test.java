package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.account.ProviderAccount;
import gr.aegean.icsd.icarus.function.Function;
import gr.aegean.icsd.icarus.user.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "test")
public class Test {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Test name cannot be blank")
    @Size(min = minLength, max = maxLength, message = "Test name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = minLength, max = maxDescriptionLength, message = "Test description does not conform to length limitations")
    private String description;

    @NotBlank(message = "Http Method used by the Test cannot be blank")
    private String httpMethod;

    @Pattern(regexp = "^/([a-zA-Z]+/)*(\\{[a-zA-Z]+\\}/([a-zA-Z]+/)*)?$",
            message = "The exposed path is not in a valid format")
    private String path;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "test_author_id")
    private IcarusUser testAuthor;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "target_function_id")
    private Function targetFunction;

    @ManyToMany(cascade = CascadeType.REFRESH, targetEntity = ProviderAccount.class)
    private final Set<ProviderAccount> accountsList = new HashSet<>();



    public Test(String name, HttpMethod httpMethod, String description, IcarusUser author,
                Function targetFunction) {
        this.name = name;
        this.httpMethod = httpMethod.toString();
        this.description = description;
        this.testAuthor = author;
        this.targetFunction = targetFunction;
    }

    public Test(String name, HttpMethod httpMethod, String description, String path, IcarusUser author,
                Function targetFunction) {
        this.name = name;
        this.httpMethod = httpMethod.toString();
        this.description = description;
        this.path = path;
        this.testAuthor = author;
        this.targetFunction = targetFunction;
    }

    public Test() {}



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

    public Function getTargetFunction() {return this.targetFunction;}


}
