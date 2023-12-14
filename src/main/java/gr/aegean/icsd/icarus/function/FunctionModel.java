package gr.aegean.icsd.icarus.function;


import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;

public class FunctionModel  extends RepresentationModel<FunctionModel> {


    private Long id;
    private String name;
    private String description;
    private String githubURL;
    private String functionSource;

    // TODO: Replace with TestModel
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

    public String getFunctionSource() {
        return functionSource;
    }

    public void setFunctionSource(String functionSource) {
        this.functionSource = functionSource;
    }

    public Set<Long> getCreatedTests() {
        return createdTests;
    }

    public void setCreatedTests(Set<Long> createdTests) {
        this.createdTests = createdTests;
    }


}
