package gr.aegean.icsd.icarus.provideraccount;

import gr.aegean.icsd.icarus.util.security.UserUtils;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class ProviderAccountModelAssembler extends RepresentationModelAssemblerSupport<ProviderAccount, ProviderAccountModel> {


    public ProviderAccountModelAssembler() {
        super(ProviderAccountController.class, ProviderAccountModel.class);
    }


    @NonNull
    public ProviderAccountModel toModel(@NonNull AwsAccount entity) {

        Long id = entity.getId();
        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String awsAccessKey = entity.getAwsAccessKey();
        String awsSecretKey = entity.getAwsSecretKey();

        ProviderAccountModel awsAccountModel = new ProviderAccountModel();

        awsAccountModel.setId(id);
        awsAccountModel.setCreator(entity.getCreator().getUsername());
        awsAccountModel.setName(accountName);
        awsAccountModel.setDescription(accountDescription);
        awsAccountModel.setAwsAccessKey(awsAccessKey);
        awsAccountModel.setAwsSecretKey(awsSecretKey);

        awsAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .updateAwsAccount(UserUtils.getUsername(), entity.getName(), awsAccountModel))
                .withRel("Update")

        );

        awsAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .detachProviderAccount(UserUtils.getUsername(), entity.getName()))
                .withRel("Detach")
        );

        awsAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .getUsersAccounts(UserUtils.getUsername(), 0, DEFAULT_PAGE_SIZE))
                .withRel("View All Accounts")
        );

        return awsAccountModel;
    }


    @NonNull
    public ProviderAccountModel toModel(@NonNull GcpAccount entity) {

        Long id = entity.getId();
        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String gcpCredentials = entity.getGcpKeyfile();

        ProviderAccountModel gcpAccountModel = new ProviderAccountModel();

        gcpAccountModel.setId(id);
        gcpAccountModel.setCreator(entity.getCreator().getUsername());
        gcpAccountModel.setName(accountName);
        gcpAccountModel.setDescription(accountDescription);
        gcpAccountModel.setKeyfile(gcpCredentials);
        gcpAccountModel.setGcpProjectId(entity.getGcpProjectId());

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .updateGcpAccount(UserUtils.getUsername(), entity.getName(), gcpAccountModel))
                .withRel("Update")

        );

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .detachProviderAccount(UserUtils.getUsername(), entity.getName()))
                .withRel("Detach")
        );

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .getUsersAccounts(UserUtils.getUsername(), 0 , DEFAULT_PAGE_SIZE))
                .withRel("View All Accounts")
        );

        return gcpAccountModel;
    }

    
    public PagedModel<ProviderAccountModel> createPagedModel(Page<ProviderAccount> profiles, String username) {

        PagedModel<ProviderAccountModel> pagedModel = createPagedModelFromPage(profiles);

        pagedModel.add(linkTo(methodOn(ProviderAccountController.class).getUsersAccounts(username,
                profiles.getNumber(), profiles.getSize())).withSelfRel());

        if (profiles.hasNext()) {
            pagedModel.add(linkTo(methodOn(ProviderAccountController.class).getUsersAccounts(username,
                    profiles.getNumber() + 1, profiles.getSize())).withRel("next"));
        }

        if (profiles.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(ProviderAccountController.class).getUsersAccounts(username,
                    profiles.getNumber() - 1, profiles.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<ProviderAccountModel> createPagedModelFromPage (Page<ProviderAccount> accountsPage) {

        List<ProviderAccountModel> providerAccountModels = accountsPage.getContent().stream().map(this::toModel).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (accountsPage.getSize(), accountsPage.getNumber(), accountsPage.getTotalElements());

        return PagedModel.of(providerAccountModels, pageMetadata);
    }

    @NonNull
    @Override
    public ProviderAccountModel toModel(@NonNull ProviderAccount entity) {

        if (entity.getAccountType().equals("AwsAccount")) {
            return toModel((AwsAccount) entity);
        }

        if (entity.getAccountType().equals("GcpAccount")) {
            return toModel((GcpAccount) entity);
        }

        throw new UnsupportedOperationException("Account type of: " + entity.getAccountType() +
                " is not supported by the model assembler");
    }


}
