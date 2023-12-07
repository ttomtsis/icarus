package gr.aegean.icsd.icarus.test.performancetest.resourceconfiguration;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Entity
public class ResourceConfiguration {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resource configuration's region cannot be blank")
    private String region;

    @Min(value = 128, message = "Resource configuration's used memory cannot be less than 128MB")
    @NotNull(message = "Resource configuration's used memory cannot be null")
    private Integer usedMemory;

    @Min(value = 1, message = "Resource configuration's cpu cannot be less than 0")
    @Max(value = 8, message = "Resource configuration's cpu cannot exceed 8")
    private Integer cpu;

    @ManyToOne(targetEntity = PerformanceTest.class, optional = false)
    private PerformanceTest parentTest;



    public static ResourceConfiguration createResourceConfigurationFromModel(ResourceConfigurationModel model) {
        return new ResourceConfiguration(null, model.getRegion(), model.getUsedMemory());
    }



    public ResourceConfiguration(PerformanceTest parentTest, String region, Integer usedMemory) {
        this.region = region;
        this.usedMemory = usedMemory;
        this.parentTest = parentTest;
    }

    public ResourceConfiguration(PerformanceTest parentTest, String region, Integer usedMemory, Integer cpu) {
        this.region = region;
        this.usedMemory = usedMemory;
        this.cpu = cpu;
        this.parentTest = parentTest;
    }

    public ResourceConfiguration() {}



    public void setId(Long id) {
        this.id = id;
    }

    public void setParentTest(PerformanceTest parentTest) {
        this.parentTest = parentTest;
    }

    public Long getId() {
        return id;
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

    public PerformanceTest getParentTest() {
        return parentTest;
    }


}
