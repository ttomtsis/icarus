package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import gr.aegean.icsd.icarus.util.exceptions.LoadProfileNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.TestNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Service
public class LoadProfileService {


    private final TestRepository testRepository;
    private final LoadProfileRepository loadProfileRepository;



    public LoadProfileService(TestRepository testRepository, LoadProfileRepository repository) {
        this.testRepository = testRepository;
        this.loadProfileRepository = repository;
    }



    public LoadProfile createLoadProfile(@NotNull LoadProfile newLoadProfile, @NotNull @Positive Long testId) {

        PerformanceTest parentTest = checkIfTestExists(testId);

        newLoadProfile.setParentTest(parentTest);

        return loadProfileRepository.save(newLoadProfile);

    }

    public void deleteLoadProfile(@NotNull @Positive Long testId, @NotNull @Positive Long loadProfileId) {

        checkIfTestExists(testId);

        checkIfProfileExists(loadProfileId);

        loadProfileRepository.deleteById(loadProfileId);
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

    private void setIfNotNull(Consumer<Integer> setter, Integer value) {

        if (value != null && value >= 0) {
            setter.accept(value);
        }
    }

    public Page<LoadProfile> getLoadProfiles(@NotNull @Positive Long testId, @NotNull Pageable pageable) {

        checkIfTestExists(testId);

        PerformanceTest parentTest = new PerformanceTest();
        parentTest.setId(testId);

        return loadProfileRepository.findAllByParentTest(parentTest, pageable);
    }

    private PerformanceTest checkIfTestExists(Long parentTestId) {

        return (PerformanceTest) testRepository.findById(parentTestId)
                .orElseThrow( () -> new TestNotFoundException(parentTestId));
    }

    private LoadProfile checkIfProfileExists(Long loadProfileId) {

        return loadProfileRepository.findById(loadProfileId)
                .orElseThrow( () -> new LoadProfileNotFoundException(loadProfileId));
    }


}
