package gr.aegean.icsd.icarus.test.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.util.exceptions.ResourceConfigurationNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ResourceConfigurationService {


    private final ResourceConfigurationRepository resourceConfigurationRepository;
    private final TestRepository testRepository;



    public ResourceConfigurationService(ResourceConfigurationRepository repository, TestRepository testRepository) {
        this.resourceConfigurationRepository = repository;
        this.testRepository = testRepository;
    }



    public Page<ResourceConfiguration> getResourceConfigurations(@NotNull @Positive Long testId,
                                                                      @NotNull Pageable pageable) {

        Test parentTest = checkIfTestExists(testId);

        return resourceConfigurationRepository.findAllByParentTest(parentTest, pageable);
    }

    public ResourceConfiguration createConfiguration(@NotNull ResourceConfiguration newConfiguration,
                                                     @NotNull @Positive Long testId) {

        Test parentTest = checkIfTestExists(testId);

        newConfiguration.setParentTest(parentTest);
        return resourceConfigurationRepository.save(newConfiguration);
    }

    public void updateConfiguration(Long testId, Long configurationId, ResourceConfigurationModel model) {

        checkIfTestExists(testId);

        ResourceConfiguration existingResourceConfiguration = checkIfConfigurationExists(configurationId);

        if (StringUtils.isNotBlank(model.getRegion())) {
            existingResourceConfiguration.setRegion(model.getRegion());
        }

        if (model.getPlatform() != null && !model.getPlatform().toString().isBlank()) {
            existingResourceConfiguration.setProviderPlatform(model.getPlatform());
        }

        if (model.getUsedMemory() != null && model.getUsedMemory() >= 0) {
            existingResourceConfiguration.setUsedMemory(model.getUsedMemory());
        }

        if (model.getCpu() != null && model.getCpu() >= 0) {
            existingResourceConfiguration.setCpu(model.getCpu());
        }

        resourceConfigurationRepository.save(existingResourceConfiguration);
    }

    public void deleteConfiguration(Long testId, Long configurationId) {

        checkIfTestExists(testId);

        ResourceConfiguration existingResourceConfiguration = checkIfConfigurationExists(configurationId);

        resourceConfigurationRepository.delete(existingResourceConfiguration);
    }


    private Test checkIfTestExists(Long parentTestId) {

        return testRepository.findById(parentTestId)
                .orElseThrow( () -> new TestNotFoundException(parentTestId));
    }

    private ResourceConfiguration checkIfConfigurationExists(Long configurationId) {

        return resourceConfigurationRepository.findById(configurationId)
                .orElseThrow( () -> new ResourceConfigurationNotFoundException(configurationId));
    }


}
