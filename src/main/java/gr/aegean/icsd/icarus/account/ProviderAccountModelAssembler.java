package gr.aegean.icsd.icarus.account;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;


@Component
public class ProviderAccountModelAssembler extends RepresentationModelAssemblerSupport<ProviderAccount, ProviderAccountModel> {


    public ProviderAccountModelAssembler() {
        super(ProviderAccountController.class, ProviderAccountModel.class);
    }


    public ProviderAccountModel toModel(AwsAccount entity) {

        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String awsAccessKey = entity.getAwsAccessKey();
        String awsSecretKey = entity.getAwsSecretKey();

        ProviderAccountModel awsAccountModel = new ProviderAccountModel();

        awsAccountModel.setName(accountName);
        awsAccountModel.setDescription(accountDescription);
        awsAccountModel.setAwsAccessKey(awsAccessKey);
        awsAccountModel.setAwsSecretKey(awsSecretKey);

        /*awsAccountModel.add(linkTo(methodOn(ProviderAccountModel.class)
                .showAllCommentsForAStory(parentStoryID, 0, 10)).withRel("Update "));*/

        return awsAccountModel;
    }

    public ProviderAccountModel toModel(GcpAccount entity) {

        String accountName = entity.getName();
        String accountDescription = entity.getDescription();

        String gcpCredentials = entity.getGcpKeyfile();

        ProviderAccountModel gcpAccountModel = new ProviderAccountModel();

        gcpAccountModel.setName(accountName);
        gcpAccountModel.setDescription(accountDescription);
        gcpAccountModel.setKeyfile(gcpCredentials);

        /*awsAccountModel.add(linkTo(methodOn(ProviderAccountModel.class)
                .showAllCommentsForAStory(parentStoryID, 0, 10)).withRel("Update "));*/

        return gcpAccountModel;
    }

    @Override
    public ProviderAccountModel toModel(ProviderAccount entity) {
        return null;
    }


}
