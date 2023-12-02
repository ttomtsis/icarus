package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.annotations.GithubUrl.GithubUrl;
import gr.aegean.icsd.icarus.util.annotations.ValidFilePath.ValidFilePath;
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
    @Size(min = minLength, max = maxLength, message = "Function name does not conform to length limitations")
    @Column(unique = true)
    private String name;

    @Size(min = minLength, max = maxDescriptionLength,
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

    public void addTest(Test newTest) {
        createdTests.add(newTest);
    }


}
