package gr.aegean.icsd.icarus.account;

import gr.aegean.icsd.icarus.util.configuration.security.UserUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class ProviderAccountModelAssembler extends RepresentationModelAssemblerSupport<ProviderAccount, ProviderAccountModel> {


    public ProviderAccountModelAssembler() {
        super(ProviderAccountController.class, ProviderAccountModel.class);
    }


    @NonNull
    public ProviderAccountModel toModel(@NonNull AwsAccount entity) {

        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String awsAccessKey = entity.getAwsAccessKey();
        String awsSecretKey = entity.getAwsSecretKey();

        ProviderAccountModel awsAccountModel = new ProviderAccountModel();

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
                .getUsersAccounts(UserUtils.getUsername()))
                .withRel("View All Accounts")
        );

        return awsAccountModel;
    }


    @NonNull
    public ProviderAccountModel toModel(@NonNull GcpAccount entity) {

        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String gcpCredentials = entity.getGcpKeyfile();

        ProviderAccountModel gcpAccountModel = new ProviderAccountModel();

        gcpAccountModel.setName(accountName);
        gcpAccountModel.setDescription(accountDescription);
        gcpAccountModel.setKeyfile(gcpCredentials);

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .updateGcpAccount(UserUtils.getUsername(), entity.getName(), gcpAccountModel))
                .withRel("Update")

        );

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .detachProviderAccount(UserUtils.getUsername(), entity.getName()))
                .withRel("Detach")
        );

        gcpAccountModel.add(linkTo(methodOn(ProviderAccountController.class)
                .getUsersAccounts(UserUtils.getUsername()))
                .withRel("View All Accounts")
        );

        return gcpAccountModel;
    }


    @NonNull
    @Override
    public ProviderAccountModel toModel(@NonNull ProviderAccount entity) {
        return null;
    }


}
