package gr.aegean.icsd.icarus.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping(value = "api/v0/users", produces = "application/json")
public class IcarusUserController {


    private final IcarusUserService service;
    private final IcarusUserModelAssembler modelAssembler;



    public IcarusUserController(IcarusUserService service, IcarusUserModelAssembler modelAssembler) {
        this.service = service;
        this.modelAssembler = modelAssembler;
    }



    @GetMapping
    public ResponseEntity<IcarusUserModel> viewAccount() {

        IcarusUser loggedInUser = service.viewUserAccount();
        IcarusUserModel userModel = modelAssembler.toModel(loggedInUser);

        return ResponseEntity.ok().body(userModel);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {

        service.deleteUserAccount();

        return ResponseEntity.noContent().build();
    }


    @PutMapping
    public ResponseEntity<Void> updateAccount(@RequestBody IcarusUserModel icarusUserModel) {

        IcarusUser updatedUser = IcarusUser.createUserFromModel(icarusUserModel);
        service.updateUserAccount(updatedUser);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/register")
    public ResponseEntity<IcarusUserModel> registerUser(@RequestBody IcarusUserModel icarusUserModel) {

        IcarusUser newUser = IcarusUser.createUserFromModel(icarusUserModel);

        IcarusUser savedUser = service.createUserAccount(newUser);

        IcarusUserModel savedUserModel = modelAssembler.toModel(savedUser);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/users/" + savedUser.getId())
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).body(savedUserModel);
    }


    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody String icarusUserEmail) {

        service.resetAccountPassword(icarusUserEmail);

        return ResponseEntity.noContent().build();
    }


}
