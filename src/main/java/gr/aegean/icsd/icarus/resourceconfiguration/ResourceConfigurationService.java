package gr.aegean.icsd.icarus.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.util.exceptions.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class ResourceConfigurationService {


    private final ResourceConfigurationRepository resourceConfigurationRepository;
    private final TestRepository testRepository;



    public ResourceConfigurationService(ResourceConfigurationRepository repository, TestRepository testRepository) {
        this.resourceConfigurationRepository = repository;
        this.testRepository = testRepository;
    }



    public Page<ResourceConfiguration> getResourceConfigurations(@NotNull @Positive Long testId,
                                                                      @NotNull Pageable pageable) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        Test parentTest = checkIfTestExists(testId);

        return resourceConfigurationRepository.findAllByParentTestAndCreator(parentTest, loggedInUser, pageable);
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

        if (model.getRegions() != null) {
            existingResourceConfiguration.setRegions(model.getRegions());
        }

        if (model.getPlatform() != null && !model.getPlatform().toString().isBlank()) {
            existingResourceConfiguration.setProviderPlatform(model.getPlatform());
        }

        if (model.getMemoryConfigurations() != null) {
            existingResourceConfiguration.setMemoryConfigurations(model.getMemoryConfigurations());
        }

        if (model.getCpuConfigurations() != null) {
            existingResourceConfiguration.setCpuConfigurations(model.getCpuConfigurations());
        }

        if (StringUtils.isNotBlank(model.getFunctionRuntime())) {
            existingResourceConfiguration.setFunctionRuntime(model.getFunctionRuntime());
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
                .orElseThrow( () -> new EntityNotFoundException(Test.class, parentTestId));
    }

    private ResourceConfiguration checkIfConfigurationExists(Long configurationId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();

        return resourceConfigurationRepository.findResourceConfigurationByIdAndCreator(configurationId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(ResourceConfigurationService.class, configurationId));
    }


}
