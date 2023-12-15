package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.annotations.GithubUrl.GithubUrl;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
import gr.aegean.icsd.icarus.util.exceptions.FunctionConfigurationException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
public class Function {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Function's name cannot be blank")
    @Size(min = MIN_LENGTH, max = MAX_LENGTH, message = "Function name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH,
            message = "Function's description does not conform to length limitations")
    private String description;

    @GithubUrl(message = "Function's GitHub URL is not valid ")
    private String githubURL;

    @ValidFilePath(message = "Function's source is not a valid filepath")
    private String functionSource;

    @OneToMany(mappedBy = "targetFunction", targetEntity = Test.class, cascade = CascadeType.REFRESH)
    private final Set<Test> createdTests = new HashSet<>();



    public Function (String name, String description, String githubURL) {
        this.name = name;
        this.description = description;
        this.githubURL = githubURL;
    }

    public Function (String name, String description, File functionSource) {
        this.name = name;
        this.description = description;
        this.functionSource = functionSource.getAbsolutePath();
    }

    public Function() {}


    public static Function createFunctionFromModel(FunctionModel model) {

        if (StringUtils.isBlank(model.getGithubURL()) && StringUtils.isBlank(model.getFunctionSource())) {
            throw new FunctionConfigurationException("Function requires either a GitHub URL or a Filepath to the local" +
                    "source");
        }

        if (StringUtils.isNotBlank(model.getGithubURL()) && StringUtils.isNotBlank(model.getFunctionSource())) {
            throw new FunctionConfigurationException("A function can either fetch it's source code from the local" +
                    "filesystem or from github, not from both");
        }

        if (model.getGithubURL() == null) {
            return new Function(model.getName(), model.getDescription(), model.getFunctionSource());
        }

        return new Function(model.getName(), model.getDescription(), model.getGithubURL());
    }



    @PrePersist
    private void checkFunctionSource() {

        if (StringUtils.isNotBlank(this.functionSource) &&
                StringUtils.isNotBlank(this.githubURL)) {

            throw new FunctionConfigurationException("A function can either fetch it's source code from the local" +
                    "filesystem or from github, not from both");
        }
    }

    @PreRemove
    private void removeForeignKeyConstraints() {

        for (Test test : this.createdTests) {
            test.setTargetFunction(null);
        }
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

    public String getFunctionSource() {
        return functionSource;
    }

    public void setFunctionSource(String functionSource) {
        this.functionSource = functionSource;
    }

    public Set<Test> getCreatedTests() {
        return createdTests;
    }


}
