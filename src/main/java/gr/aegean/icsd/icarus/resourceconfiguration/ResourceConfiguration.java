package gr.aegean.icsd.icarus.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.aws.LambdaRuntime;
import gr.aegean.icsd.icarus.util.enums.Platform;
import gr.aegean.icsd.icarus.util.exceptions.resourceconfiguration.InvalidResourceConfigurationConfigurationException;
import gr.aegean.icsd.icarus.util.gcp.GcfRuntime;
import gr.aegean.icsd.icarus.util.gcp.GcpRegion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;


@Entity
@Table(name = "resource_configuration")
public class ResourceConfiguration {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_configuration_regions", joinColumns = @JoinColumn(name = "id"))
    private Set<String> regions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_configuration_memory", joinColumns = @JoinColumn(name = "id"))
    private Set<Integer> memoryConfigurations = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_configuration_cpu", joinColumns = @JoinColumn(name = "id"))
    private Set<Integer> cpuConfigurations = new HashSet<>();

    @NotBlank(message = "Resource configuration's function runtime cannot be blank")
    private String functionRuntime;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Resource configuration's target platform cannot be blank")
    private Platform providerPlatform;

    @ManyToOne(targetEntity = Test.class, optional = false)
    private Test parentTest;



    public static ResourceConfiguration createResourceConfigurationFromModel(ResourceConfigurationModel model) {

        if (model.getCpuConfigurations() == null) {

            return new ResourceConfiguration(null, model.getRegions(),
                    model.getMemoryConfigurations(), model.getPlatform(), model.getFunctionRuntime());
        }

        return new ResourceConfiguration(null, model.getRegions(),
                model.getMemoryConfigurations(), model.getCpuConfigurations(), model.getPlatform(),
                model.getFunctionRuntime());
    }



    public ResourceConfiguration(Test parentTest, Set<String> region, Set<Integer> memoryConfigurations,
                                 Platform targetPlatform, String functionRuntime) {

        this.regions = region;
        this.memoryConfigurations = memoryConfigurations;
        this.parentTest = parentTest;
        this.providerPlatform = targetPlatform;
        this.functionRuntime = functionRuntime;
    }

    public ResourceConfiguration(Test parentTest, Set<String> region, Set<Integer> memoryConfigurations,
                                 Set<Integer> cpuConfigurations, Platform targetPlatform,
                                 String functionRuntime) {

        this.regions = region;
        this.memoryConfigurations = memoryConfigurations;
        this.cpuConfigurations = cpuConfigurations;
        this.parentTest = parentTest;
        this.providerPlatform = targetPlatform;
        this.functionRuntime = functionRuntime;
    }

    public ResourceConfiguration() {}



    @PrePersist
    private void validateResourceConfiguration() {

        validateCpuConfigurations();
        validateMemoryConfigurations();
        validateRegionsList();
        validateFunctionRuntime();
    }

    private void validateCpuConfigurations() {

        if (this.providerPlatform.equals(Platform.AWS) && !this.cpuConfigurations.isEmpty()) {

            throw new InvalidResourceConfigurationConfigurationException("AWS does not support CPU configurations");
        }

        for (Integer cpuConfiguration : this.cpuConfigurations) {

            if (cpuConfiguration < GCP_MIN_CPU || cpuConfiguration > GCP_MAX_CPU) {
                throw new InvalidResourceConfigurationConfigurationException("GCP CPU configurations can only range from " +
                        GCP_MIN_CPU + " to " + GCP_MAX_CPU);
            }
        }
    }

    private void validateRegionsList() {

        if (this.regions.isEmpty()) {
            throw new InvalidResourceConfigurationConfigurationException
                    ("At least one region must be provided per resource configuration");
        }

        for (String region : this.regions) {

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
    }

    private void validateFunctionRuntime() {

        if (this.providerPlatform.equals(Platform.GCP)) {

            try {
                GcfRuntime.valueOf(functionRuntime);
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidResourceConfigurationConfigurationException
                        ("The provided function runtime: " + functionRuntime + " is not a valid GCF runtime");
            }
        }

        if (this.providerPlatform.equals(Platform.AWS)) {

            try {
                LambdaRuntime.valueOf(functionRuntime);
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidResourceConfigurationConfigurationException
                        ("The provided function runtime: " + functionRuntime + " is not a valid Lambda runtime");
            }
        }
    }

    private void validateMemoryConfigurations() {

        if (this.memoryConfigurations == null || this.memoryConfigurations.isEmpty()) {

            throw new InvalidResourceConfigurationConfigurationException
                    ("At least one memory configuration must be provided per resource configuration");
        }

        for (Integer memoryConfiguration : this.memoryConfigurations) {

            if (memoryConfiguration < FUNCTION_MEMORY_MIN) {

                throw new InvalidResourceConfigurationConfigurationException
                        ("Provided memory configurations must be greater than the required minimum of: "
                        + FUNCTION_MEMORY_MIN);
            }
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getRegions() {
        return regions;
    }

    public void setRegions(Set<String> regions) {
        this.regions = regions;
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

    public String getFunctionRuntime() {
        return functionRuntime;
    }

    public void setFunctionRuntime(String functionRuntime) {
        this.functionRuntime = functionRuntime;
    }

    public Platform getProviderPlatform() {
        return providerPlatform;
    }

    public void setProviderPlatform(Platform providerPlatform) {
        this.providerPlatform = providerPlatform;
    }

    public Test getParentTest() {
        return parentTest;
    }

    public void setParentTest(Test parentTest) {
        this.parentTest = parentTest;
    }


}
