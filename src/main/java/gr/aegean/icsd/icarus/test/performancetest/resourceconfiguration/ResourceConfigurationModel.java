package gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration;


import org.springframework.hateoas.RepresentationModel;

public class ResourceConfigurationModel extends RepresentationModel<ResourceConfigurationModel> {


    private String region;
    private Integer usedMemory;
    private Integer cpu;
    private Long parentTest;



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
