package gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration;


import gr.aegean.icsd.icarus.util.enums.Platform;
import org.springframework.hateoas.RepresentationModel;

public class ResourceConfigurationModel extends RepresentationModel<ResourceConfigurationModel> {


    private Long id;
    private String region;
    private Integer usedMemory;
    private Integer cpu;
    private Long parentTest;
    private Platform platform;



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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Integer usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Long getParentTest() {
        return parentTest;
    }

    public void setParentTest(Long parentTest) {
        this.parentTest = parentTest;
    }


}
