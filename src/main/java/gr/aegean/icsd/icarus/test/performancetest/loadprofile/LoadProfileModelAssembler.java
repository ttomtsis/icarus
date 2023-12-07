package gr.aegean.icsd.icarus.test.performancetest.loadprofile;


import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class LoadProfileModelAssembler
        extends RepresentationModelAssemblerSupport<LoadProfile, LoadProfileModel> {


    public LoadProfileModelAssembler() {
        super(LoadProfileController.class, LoadProfileModel.class);
    }




    @Override
    public LoadProfileModel toModel(LoadProfile entity) {
        return null;
    }


}
