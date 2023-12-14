package gr.aegean.icsd.icarus.test.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.InvalidResourceConfigurationConfigurationException;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
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

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Resource configuration's target platform cannot be blank")
    private Platform providerPlatform;

    @ManyToOne(targetEntity = PerformanceTest.class, optional = false)
    private Test parentTest;



    public static ResourceConfiguration createResourceConfigurationFromModel(ResourceConfigurationModel model) {

        if (model.getCpu() == null) {

            return new ResourceConfiguration(null, model.getRegion(),
                    model.getUsedMemory(), model.getPlatform());
        }

        return new ResourceConfiguration(null, model.getRegion(),
                model.getUsedMemory(), model.getCpu(), model.getPlatform());
    }



    public ResourceConfiguration(Test parentTest, String region,
                                 Integer usedMemory, Platform targetPlatform) {

        this.region = region;
        this.usedMemory = usedMemory;
        this.parentTest = parentTest;
        this.providerPlatform = targetPlatform;
    }

    public ResourceConfiguration(Test parentTest, String region,
                                 Integer usedMemory, Integer cpu, Platform targetPlatform) {

        this.region = region;
        this.usedMemory = usedMemory;
        this.cpu = cpu;
        this.parentTest = parentTest;
        this.providerPlatform = targetPlatform;
    }

    public ResourceConfiguration() {}



    @PrePersist
    private void validateConfiguration() {

        if (this.providerPlatform.equals(Platform.AWS) && this.cpu != null) {

            throw new InvalidResourceConfigurationConfigurationException("AWS does not support CPU configurations");
        }

        if (this.providerPlatform.equals(Platform.GCP)) {

            try {
                GcpRegion.valueOf(region);
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidResourceConfigurationConfigurationException
                        ("The provided region: " + region + " is not a valid GCP region");
            }
        }

        if (this.providerPlatform.equals(Platform.AWS)) {

            try {
                AwsRegion.valueOf(region);
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidResourceConfigurationConfigurationException
                        ("The provided region: " + region + " is not a valid AWS region");
            }
        }

    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setParentTest(Test parentTest) {
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

    public Test getParentTest() {
        return parentTest;
    }

    public Platform getProviderPlatform() {
        return providerPlatform;
    }

    public void setProviderPlatform(Platform providerPlatform) {
        this.providerPlatform = providerPlatform;
    }


}
