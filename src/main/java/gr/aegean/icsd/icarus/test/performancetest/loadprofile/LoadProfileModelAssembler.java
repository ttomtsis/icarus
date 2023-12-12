package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@Component
public class LoadProfileModelAssembler
        extends RepresentationModelAssemblerSupport<LoadProfile, LoadProfileModel> {


    public LoadProfileModelAssembler() {
        super(LoadProfileController.class, LoadProfileModel.class);
    }




    @Override
    @NotNull
    public LoadProfileModel toModel(LoadProfile entity) {

        LoadProfileModel newModel = new LoadProfileModel();

        newModel.setId(entity.getId());
        newModel.setParentTest(entity.getParentTest().getId());
        newModel.setLoadTime(entity.getLoadTime());
        newModel.setRampUp(entity.getRampUp());
        newModel.setConcurrentUsers(entity.getConcurrentUsers());
        newModel.setStartDelay(entity.getStartDelay());
        newModel.setThinkTime(entity.getThinkTime());

        return addLinks(newModel);
    }

    public PagedModel<LoadProfileModel> createPagedModel(Page<LoadProfile> profiles, Long testId) {
        PagedModel<LoadProfileModel> pagedModel = createPagedModelFromPage(profiles);

        pagedModel.add(linkTo(methodOn(LoadProfileController.class).getAllLoadProfiles(testId,
                profiles.getNumber(), profiles.getSize())).withSelfRel());

        if (profiles.hasNext()) {
            pagedModel.add(linkTo(methodOn(LoadProfileController.class).getAllLoadProfiles(testId,
                    profiles.getNumber() + 1, profiles.getSize())).withRel("next"));
        }

        if (profiles.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(LoadProfileController.class).getAllLoadProfiles(testId,
                    profiles.getNumber() - 1, profiles.getSize())).withRel("previous"));
        }

        return pagedModel;
    }

    private PagedModel<LoadProfileModel> createPagedModelFromPage ( Page<LoadProfile> loadProfilePage ) {

        List<LoadProfileModel> loadProfileModels = loadProfilePage.getContent().stream().map(this::toModel).toList();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata
                (loadProfilePage.getSize(), loadProfilePage.getNumber(), loadProfilePage.getTotalElements());

        return PagedModel.of(loadProfileModels, pageMetadata);
    }

    public LoadProfileModel addLinks(LoadProfileModel model) {

        model.add(linkTo(methodOn(LoadProfileController.class)
                .deleteLoadProfile(model.getParentTest(), model.getId())).withRel("Delete"));

        model.add(linkTo(methodOn(LoadProfileController.class)
                .updateLoadProfile(model.getParentTest(), model.getId(), new LoadProfileModel())).withRel("Update"));

        model.add(linkTo(methodOn(LoadProfileController.class)
                .getAllLoadProfiles(model.getParentTest(), 0, 10)).withRel("Get all models for this test"));

        return model;
    }


}
