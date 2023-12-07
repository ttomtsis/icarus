package gr.aegean.icsd.icarus.test.performancetest.loadprofile;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v0/tests/performance/{testId}", produces = "application/json")
public class LoadProfileController {


    private final LoadProfileService service;
    private final LoadProfileModelAssembler modelAssembler;




    public LoadProfileController(LoadProfileService service, LoadProfileModelAssembler modelAssembler) {
        this.service = service;
        this.modelAssembler = modelAssembler;
    }



    @PostMapping(produces = "application/json")
    public ResponseEntity<LoadProfileModel> createLoadProfile(@PathVariable Long testId,
                                                              @RequestBody LoadProfileModel loadModel) {

        LoadProfile newLoadProfile = LoadProfile.createLoadProfileFromModel(loadModel);

        //service.createLoadProfile(newTest);

        return null;
    }
}
