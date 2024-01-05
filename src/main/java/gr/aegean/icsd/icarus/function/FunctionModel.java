package gr.aegean.icsd.icarus.function;


import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;

public class FunctionModel  extends RepresentationModel<FunctionModel> {


    private Long id;
    private String name;
    private String author;
    private String description;
    private String githubURL;
    private String functionHandler;
    private String functionSourceDirectory;
    private String functionSourceFileName;
    private Set<Long> createdTests = new HashSet<>();



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

    public Set<Long> getCreatedTests() {
        return createdTests;
    }

    public void setCreatedTests(Set<Long> createdTests) {
        this.createdTests = createdTests;
    }

    public String getFunctionHandler() {
        return functionHandler;
    }

    public void setFunctionHandler(String functionHandler) {
        this.functionHandler = functionHandler;
    }

    public String getFunctionSourceFileName() {
        return functionSourceFileName;
    }

    public void setFunctionSourceFileName(String functionSourceFileName) {
        this.functionSourceFileName = functionSourceFileName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


}
