package gr.aegean.icsd.icarus.testexecution;

import gr.aegean.icsd.icarus.testexecution.metricresult.MetricResult;
import gr.aegean.icsd.icarus.testexecution.testcaseresult.TestCaseResult;
import gr.aegean.icsd.icarus.util.enums.ExecutionState;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class TestExecutionModelAssembler extends RepresentationModelAssemblerSupport<TestExecution, TestExecutionModel> {


    public TestExecutionModelAssembler() {
        super(TestExecutionController.class, TestExecutionModel.class);
    }


    @Override
    public @NotNull TestExecutionModel toModel(TestExecution entity) {
        
        TestExecutionModel newModel = new TestExecutionModel();

        newModel.setId(entity.getId());
        newModel.setCreator(entity.getCreator().getUsername());

        if(entity.getReport() != null) {
            newModel.setReport(entity.getReport().getId());
        }

        if(entity.getEndDate() == null && entity.getState().equals(ExecutionState.ERROR)) {
            newModel.setEndDate("Execution did not complete because of an error");
        }
        else if(entity.getEndDate() == null && !entity.getState().equals(ExecutionState.ERROR)) {
            newModel.setEndDate("Execution has not yet completed");
        }
        else {
            newModel.setEndDate(formatInstant(entity.getEndDate()));
        }

        newModel.setStartDate(formatInstant(entity.getStartDate()));
        newModel.setParentTest(entity.getParentTest().getId());
        newModel.setTestState(entity.getState());

        Set<Long> testCaseResults = new HashSet<>();
        for (TestCaseResult result : entity.getTestCaseResults()) {

            testCaseResults.add(result.getId());
        }

        Set<Long> metricResults = new HashSet<>();
        for (MetricResult result : entity.getMetricResults()) {

            metricResults.add(result.getId());
        }

        newModel.setTestCaseResults(testCaseResults);
        newModel.setMetricResults(metricResults);

        return addLinks(newModel);
    }


    public PagedModel<TestExecutionModel> createPagedModel(Page<TestExecution> executions,
                                                                   Long testId) {

        PagedModel<TestExecutionModel> pagedModel = createPagedModelFromPage(executions);

        pagedModel.add(linkTo(methodOn(TestExecutionController.class).getAllExecutions(testId,
                executions.getNumber(), executions.getSize())).withSelfRel());

        if (executions.hasNext()) {
            pagedModel.add(linkTo(methodOn(TestExecutionController.class).getAllExecutions(testId,
                    executions.getNumber() + 1, executions.getSize())).withRel("next"));
        }

        if (executions.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(TestExecutionController.class).getAllExecutions(testId,
                    executions.getNumber() - 1, executions.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<TestExecutionModel> createPagedModelFromPage(Page<TestExecution> configurationPage) {

        List<TestExecutionModel> configurationModels = configurationPage.getContent().stream()
                .map(this::toModel).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (configurationPage.getSize(), configurationPage.getNumber(), configurationPage.getTotalElements());

        return PagedModel.of(configurationModels, pageMetadata);
    }


    public TestExecutionModel addLinks(TestExecutionModel model) {

        model.add(linkTo(methodOn(TestExecutionController.class)
                .deleteExecution(model.getParentTest(), model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(TestExecutionController.class)
                .getExecution(model.getParentTest(), model.getId())).withSelfRel());

        model.add(linkTo(methodOn(TestExecutionController.class)
                .getAllExecutions(model.getParentTest(), 0, DEFAULT_PAGE_SIZE))
                .withRel("Get all Test executions for this test"));

        return model;
    }
    
    
    private String formatInstant(Instant instant) {

        return DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
    
    
}
