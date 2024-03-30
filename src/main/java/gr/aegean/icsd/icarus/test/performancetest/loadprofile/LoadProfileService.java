package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTestRepository;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.interfaces.UtilitiesInterface;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class LoadProfileService implements UtilitiesInterface {


    private final PerformanceTestRepository testRepository;
    private final LoadProfileRepository loadProfileRepository;



    public LoadProfileService(PerformanceTestRepository testRepository, LoadProfileRepository repository) {
        this.testRepository = testRepository;
        this.loadProfileRepository = repository;
    }



    public LoadProfile createLoadProfile(@NotNull LoadProfile newLoadProfile, @NotNull @PositiveOrZero Long testId) {

        PerformanceTest parentTest = checkIfTestExists(testId);
        newLoadProfile.setParentTest(parentTest);

        return loadProfileRepository.save(newLoadProfile);

    }


    public void deleteLoadProfile(@NotNull @Positive Long testId, @NotNull @Positive Long loadProfileId) {

        checkIfTestExists(testId);

        LoadProfile existingLoadProfile = checkIfProfileExists(loadProfileId);

        loadProfileRepository.delete(existingLoadProfile);
    }


    public void updateLoadModel(@NotNull @Positive Long testId, @NotNull @Positive Long loadProfileId,
                                @NotNull LoadProfileModel model) {

        checkIfTestExists(testId);

        LoadProfile existingLoadProfile = checkIfProfileExists(loadProfileId);

        setIfNotNull(existingLoadProfile::setLoadTime, model.getLoadTime());
        setIfNotNull(existingLoadProfile::setRampUp, model.getRampUp());
        setIfNotNull(existingLoadProfile::setConcurrentUsers, model.getConcurrentUsers());
        setIfNotNull(existingLoadProfile::setStartDelay, model.getStartDelay());
        setIfNotNull(existingLoadProfile::setThinkTime, model.getLoadTime());

        loadProfileRepository.save(existingLoadProfile);
    }


    public Page<LoadProfile> getLoadProfiles(@NotNull @Positive Long testId, @NotNull Pageable pageable) {

        PerformanceTest parentTest = checkIfTestExists(testId);

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return loadProfileRepository.findAllByParentTestAndCreator(parentTest, loggedInUser, pageable);
    }



    private PerformanceTest checkIfTestExists(Long parentTestId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return testRepository.findPerformanceTestByIdAndCreator(parentTestId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(PerformanceTest.class, parentTestId));
    }

    private LoadProfile checkIfProfileExists(Long loadProfileId) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        return loadProfileRepository.findByIdAndAndCreator(loadProfileId, loggedInUser)
                .orElseThrow( () -> new EntityNotFoundException(LoadProfile.class, loadProfileId));
    }


}
