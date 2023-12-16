package gr.aegean.icsd.icarus.test.resourceconfiguration;


import gr.aegean.icsd.icarus.util.enums.Platform;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;

public class ResourceConfigurationModel extends RepresentationModel<ResourceConfigurationModel> {


    private Long id;
    private Set<String> regions;
    private Set<Integer> memoryConfigurations;
    private Set<Integer> cpuConfigurations;
    private Long parentTest;
    private String functionRuntime;
    private Platform platform;



    public ResourceConfigurationModel(Long id, Platform providerPlatform, String functionRuntime,
                                      Set<String> regions, Set<Integer> cpuConfigurations,
                                      Set<Integer> memoryConfigurations, Long parentTestId) {

        this.id = id;
        this.platform = providerPlatform;
        this.functionRuntime = functionRuntime;
        this.regions = regions;
        this.cpuConfigurations = cpuConfigurations;
        this.memoryConfigurations = memoryConfigurations;
        this.parentTest = parentTestId;
    }

    public ResourceConfigurationModel() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Set<String> getRegions() {
        return regions;
    }

    public String getFunctionRuntime() {
        return functionRuntime;
    }

    public void setFunctionRuntime(String functionRuntime) {
        this.functionRuntime = functionRuntime;
    }

    public void setRegions(Set<String> newRegions) {
        this.regions = newRegions;
    }

    public Set<Integer> getMemoryConfigurations() {
        return memoryConfigurations;
    }

    public void setMemoryConfigurations(Set<Integer> memoryConfigurations) {
        this.memoryConfigurations = memoryConfigurations;
    }

    public Set<Integer> getCpuConfigurations() {
        return cpuConfigurations;
    }

    public void setCpuConfigurations(Set<Integer> cpuConfigurations) {
        this.cpuConfigurations = cpuConfigurations;
    }

    public Long getParentTest() {
        return parentTest;
    }

    public void setParentTest(Long parentTest) {
        this.parentTest = parentTest;
    }


}
