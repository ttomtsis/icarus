package gr.aegean.icsd.icarus.test.functionaltest;

import gr.aegean.icsd.icarus.test.TestRepository;
import gr.aegean.icsd.icarus.test.TestService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;


@Service
public class FunctionalTestService extends TestService {


    private final TestRepository repository;



    public FunctionalTestService(TestRepository repository) {
        super(repository);
        this.repository = repository;
    }



    public FunctionalTest createTest(@NotNull FunctionalTest newTest) {

        return (FunctionalTest) super.createTest(newTest);
    }

    @Override
    public FunctionalTest searchTest(@NotNull @Positive Long testId) {

        return (FunctionalTest) super.searchTest(testId);
    }

    public void updateTest(@NotNull @Positive Long testId, @NotNull FunctionalTestModel testModel) {

        FunctionalTest requestedTest = (FunctionalTest) super.updateTest(testId, testModel.removeLinks());

        super.setIfNotBlank(requestedTest::setFunctionURL, testModel.getFunctionUrl());

        repository.save(requestedTest);
    }


}
