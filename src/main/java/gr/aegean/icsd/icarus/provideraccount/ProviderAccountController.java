package gr.aegean.icsd.icarus.provideraccount;

import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "api/v0/users/{username}/accounts", produces = "application/json")
public class ProviderAccountController {

    private final ProviderAccountService service;
    private final ProviderAccountModelAssembler modelAssembler;


    public ProviderAccountController (ProviderAccountService service, ProviderAccountModelAssembler modelAssembler) {
        this.service = service;
        this.modelAssembler = modelAssembler;
    }


    @PreAuthorize("#username == authentication.name")
    @GetMapping
    public PagedModel<ProviderAccountModel> getUsersAccounts(@PathVariable("username") String username) {
        // TODO: Implement
        return null;
    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping(value = "/aws", consumes = "application/json")
    public ResponseEntity<ProviderAccountModel> attachAwsAccount(@PathVariable("username") String username,
                                                                 @RequestBody ProviderAccountModel awsAccountModel) {

        AwsAccount newAwsAccount = AwsAccount.createAccountFromModel(awsAccountModel);

        AwsAccount savedAwsAccount = (AwsAccount) service.attachProviderAccount(username, newAwsAccount);
        ProviderAccountModel savedAwsModel = modelAssembler.toModel(savedAwsAccount);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/users/{username}/accounts/aws/" + newAwsAccount.getName())
                .buildAndExpand(username)
                .toUri();

        return ResponseEntity.created(location).body(savedAwsModel);
    }

    @PreAuthorize("#username == authentication.name")
    @PutMapping(value = "/aws/{accountName}", consumes = "application/json")
    public ResponseEntity<Void> updateAwsAccount(@PathVariable("username") String username,
                                                 @PathVariable String accountName,
                                                 @RequestBody ProviderAccountModel awsAccountModel) {

        AwsAccount toBeUpdatedAwsAccount = AwsAccount.createAccountFromModel(awsAccountModel);

        service.updateProviderAccount(accountName, toBeUpdatedAwsAccount);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping(value = "/gcp", consumes = "application/json")
    public ResponseEntity<ProviderAccountModel> attachGcpAccount(@PathVariable("username") String username,
                                                                 @RequestBody ProviderAccountModel gcpAccountModel) {

        GcpAccount newGcpAccount = GcpAccount.createAccountFromModel(gcpAccountModel);

        GcpAccount savedGcpAccount = (GcpAccount) service.attachProviderAccount(username, newGcpAccount);
        ProviderAccountModel savedAwsModel = modelAssembler.toModel(savedGcpAccount);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/users/{username}/accounts/gcp/" + newGcpAccount.getName())
                .buildAndExpand(username)
                .toUri();

        return ResponseEntity.created(location).body(savedAwsModel);
    }

    @PreAuthorize("#username == authentication.name")
    @PutMapping(value = "/gcp/{accountName}", consumes = "application/json")
    public ResponseEntity<Void> updateGcpAccount(@PathVariable("username") String username,
                                                 @PathVariable String accountName,
                                                 @RequestBody ProviderAccountModel awsAccountModel) {

        GcpAccount toBeUpdatedGcpAccount = GcpAccount.createAccountFromModel(awsAccountModel);

        service.updateProviderAccount(accountName, toBeUpdatedGcpAccount);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("#username == authentication.name")
    @DeleteMapping("/{accountName}")
    public ResponseEntity<Void> detachProviderAccount(@PathVariable("username") String username,
                                                      @PathVariable String accountName) {

        service.detachProviderAccount(accountName);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


}
