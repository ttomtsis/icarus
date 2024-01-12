package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.annotations.GithubUrl.GithubUrl;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@EntityListeners(AuditingEntityListener.class)
public class Function {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser author;

    @NotBlank(message = "Function's name cannot be blank")
    @Size(min = MIN_LENGTH, max = MAX_LENGTH, message = "Function name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Function's description does not conform to length limitations")
    private String description;

    @GithubUrl(message = "Function's GitHub URL is not valid ")
    private String githubURL;

    @ValidFilePath(message = "Function's source directory is not a valid filepath")
    private String functionSourceDirectory;

    @Pattern(regexp = "^[^\\\\/:*?\"<>|]*$",
            message = "Function's source code archive name cannot invalid characters")
    private String functionSourceFileName;

    @NotBlank(message = "Function's handler cannot be blank")
    private String functionHandler;

    @OneToMany(mappedBy = "targetFunction", targetEntity = Test.class,
            orphanRemoval = true, cascade = CascadeType.REMOVE)
    private final Set<Test> createdTests = new HashSet<>();



    public Function (String name, String description, String functionHandler, String githubURL) {
        this.name = name;
        this.description = description;
        this.functionHandler = functionHandler;
        this.githubURL = githubURL;
    }

    public Function (String name, String description, String functionHandler) {
        this.name = name;
        this.description = description;
        this.functionHandler = functionHandler;
    }

    public Function() {}


    public static Function createFunctionFromModel(FunctionModel model) {

        if (model.getGithubURL() == null) {
            return new Function(model.getName(), model.getDescription(), model.getFunctionHandler());
        }

        return new Function(model.getName(), model.getDescription(), model.getFunctionHandler(), model.getGithubURL());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithubURL() {
        return githubURL;
    }

    public void setGithubURL(String githubURL) {
        this.githubURL = githubURL;
    }

    public String getFunctionSourceDirectory() {
        return functionSourceDirectory;
    }

    public void setFunctionSourceDirectory(String functionSourceDirectory) {
        this.functionSourceDirectory = functionSourceDirectory;
    }

    public Set<Test> getCreatedTests() {
        return createdTests;
    }

    public String getFunctionSourceFileName() {
        return functionSourceFileName;
    }

    public void setFunctionSourceFileName(String functionSourceFileName) {
        this.functionSourceFileName = functionSourceFileName;
    }

    public String getFunctionHandler() {
        return functionHandler;
    }

    public void setFunctionHandler(String functionHandler) {
        this.functionHandler = functionHandler;
    }

    public IcarusUser getAuthor() {
        return author;
    }

    public void setAuthor(IcarusUser author) {
        this.author = author;
    }


}
