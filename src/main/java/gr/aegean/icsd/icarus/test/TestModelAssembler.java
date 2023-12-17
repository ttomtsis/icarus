package gr.aegean.icsd.icarus.test;

import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
public class TestModelAssembler extends RepresentationModelAssemblerSupport<Test, TestModel> {


    public TestModelAssembler() {
        super(TestController.class, TestModel.class);
    }



    @Override
    public @NotNull TestModel toModel(Test entity) {

        TestModel newModel = new TestModel();

        newModel.setId(entity.getId());
        newModel.setName(entity.getName());
        newModel.setDescription(entity.getDescription());
        newModel.setHttpMethod(entity.getHttpMethod());

        newModel.setTestAuthor(entity.getTestAuthor().getId());

        if (entity.getTargetFunction() != null) {
            newModel.setTargetFunction(entity.getTargetFunction().getId());
        }

        newModel.setPath(entity.getPath());
        newModel.setPathVariable(entity.getPathVariable());

        Set<Long> providerAccounts = new HashSet<>();
        for (ProviderAccount account : entity.getAccountsList()) {
            providerAccounts.add(account.getId());
        }

        Set<Long> configurations = new HashSet<>();
        for (ResourceConfiguration configuration : entity.getResourceConfigurations()) {
            providerAccounts.add(configuration.getId());
        }

        newModel.setAccountsList(providerAccounts);
        newModel.setResourceConfigurations(configurations);

        return newModel;
    }


}
