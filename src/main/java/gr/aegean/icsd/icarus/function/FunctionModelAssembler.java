package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.test.Test;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class FunctionModelAssembler
        extends RepresentationModelAssemblerSupport<Function, FunctionModel> {


    public FunctionModelAssembler() {
        super(FunctionController.class, FunctionModel.class);
    }



    @Override
    @NotNull
    public FunctionModel toModel(@NotNull Function entity) {
        throw new UnsupportedOperationException("toModel(entity) in FunctionModelAssembler" +
                " should not have been invoked");
    }


    public FunctionModel toModel(Function entity, Long testId) {

        FunctionModel newModel = new FunctionModel();

        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setFunctionHandler(entity.getFunctionHandler());
        newModel.setFunctionSourceDirectory(entity.getFunctionSourceDirectory());
        newModel.setFunctionSourceFileName(entity.getFunctionSourceFileName());
        newModel.setGithubURL(entity.getGithubURL());

        Set<Long> tests = entity.getCreatedTests().stream()
                .map(Test::getId)
                .collect(Collectors.toCollection(HashSet::new));

        newModel.setCreatedTests(tests);

        return addLinks(newModel, testId);
    }

    public FunctionModel addLinks(FunctionModel model, Long testId) {

        model.add(linkTo(methodOn(FunctionController.class)
                .deleteFunction(testId, model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(FunctionController.class)
                .updateFunction(testId, model.getId(), new FunctionModel())).withRel("Update"));

        model.add(linkTo(methodOn(FunctionController.class)
                .getFunction(testId, model.getId()))
                .withSelfRel());

        return model;
    }


}
