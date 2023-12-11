package gr.aegean.icsd.icarus.test.performancetest.loadprofile;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.defaultPageSize;

@RestController
@RequestMapping(value = "api/v0/tests/performance/{testId}/load-profiles", produces = "application/json")
public class LoadProfileController {


    private final LoadProfileService service;
    private final LoadProfileModelAssembler modelAssembler;




    public LoadProfileController(LoadProfileService service, LoadProfileModelAssembler modelAssembler) {
        this.service = service;
        this.modelAssembler = modelAssembler;
    }



    @GetMapping
    public ResponseEntity<PagedModel<LoadProfileModel>> getAllLoadProfiles(@PathVariable Long testId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = defaultPageSize) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LoadProfile> profiles = service.getLoadProfiles(testId, pageable);
        PagedModel<LoadProfileModel> profilesPagedModel = modelAssembler.createPagedModel(profiles, testId);

        return ResponseEntity.ok().body(profilesPagedModel);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<LoadProfileModel> createLoadProfile(@PathVariable Long testId,
                                                              @RequestBody LoadProfileModel loadModel) {

        LoadProfile newLoadProfile = LoadProfile.createLoadProfileFromModel(loadModel);

        LoadProfile savedLoadProfile = service.createLoadProfile(newLoadProfile, testId);
        LoadProfileModel savedLoadProfileModel = modelAssembler.toModel(savedLoadProfile);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/tests/performance/" + testId + "/load-profiles/" + savedLoadProfile.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedLoadProfileModel);
    }

    @PutMapping(value = "/{loadProfileId}", consumes = "application/json")
    public ResponseEntity<Void> updateLoadProfile(@PathVariable Long testId, @PathVariable Long loadProfileId,
                                                  @RequestBody LoadProfileModel model) {

        service.updateLoadModel(testId, loadProfileId, model);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{loadProfileId}")
    public ResponseEntity<Void> deleteLoadProfile(@PathVariable Long testId, @PathVariable Long loadProfileId) {

        service.deleteLoadProfile(testId, loadProfileId);

        return ResponseEntity.noContent().build();
    }


}
