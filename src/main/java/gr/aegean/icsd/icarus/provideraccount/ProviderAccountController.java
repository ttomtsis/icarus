package gr.aegean.icsd.icarus.provideraccount;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.SerializationException;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.DEFAULT_PAGE_SIZE;

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
    public ResponseEntity<PagedModel<ProviderAccountModel>> getUsersAccounts(@PathVariable("username") String username,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProviderAccount> accounts = service.getAccounts(username, pageable);
        PagedModel<ProviderAccountModel> accountsPagedModel = modelAssembler.createPagedModel(accounts, username);

        return ResponseEntity.ok().body(accountsPagedModel);
    }


    @PreAuthorize("#username == authentication.name")
    @PostMapping(value = "/aws", consumes = "application/json")
    public ResponseEntity<ProviderAccountModel> attachAwsAccount(@PathVariable("username") String username,
                                                                 @RequestBody ProviderAccountModel awsAccountModel) {

        AwsAccount newAwsAccount = AwsAccount.createAccountFromModel(awsAccountModel);

        AwsAccount savedAwsAccount = service.attachAwsAccount(username, newAwsAccount);
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

        service.updateAwsAccount(accountName, toBeUpdatedAwsAccount);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PreAuthorize("#username == authentication.name")
    @PostMapping(value = "/gcp", consumes = "multipart/form-data")
    public ResponseEntity<ProviderAccountModel> attachGcpAccount(@PathVariable("username") String username,
                                                                 @RequestPart("gcpAccountMetadata")
                                                                    String gcpAccountModel,
                                                                 @RequestPart("gcpAccountKeyfile")
                                                                     MultipartFile gcpAccountKeyfile) {

        ProviderAccountModel serializedModel = serializeToModel(gcpAccountModel);
        GcpAccount newGcpAccount = GcpAccount.createAccountFromModel(serializedModel);

        GcpAccount savedGcpAccount = service.attachGcpAccount(username, newGcpAccount, gcpAccountKeyfile);
        ProviderAccountModel savedGcpModel = modelAssembler.toModel(savedGcpAccount);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v0/users/{username}/accounts/gcp/" + newGcpAccount.getName())
                .buildAndExpand(username)
                .toUri();

        return ResponseEntity.created(location).body(savedGcpModel);
    }


    @PreAuthorize("#username == authentication.name")
    @PutMapping(value = "/gcp/{accountName}", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateGcpAccount(@PathVariable("username") String username,
                                                 @PathVariable String accountName,
                                                 @RequestPart(name = "gcpAccountMetadata", required = false)
                                                     String gcpAccountModel,
                                                 @RequestPart(name = "gcpAccountKeyfile", required = false)
                                                     MultipartFile gcpAccountKeyfile) {

        ProviderAccountModel serializedModel = serializeToModel(gcpAccountModel);
        GcpAccount toBeUpdatedGcpAccount = GcpAccount.createAccountFromModel(serializedModel);

        service.updateGcpAccount(accountName, toBeUpdatedGcpAccount, gcpAccountKeyfile);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PreAuthorize("#username == authentication.name")
    @DeleteMapping("/{accountName}")
    public ResponseEntity<Void> detachProviderAccount(@PathVariable("username") String username,
                                                      @PathVariable String accountName) {

        service.detachProviderAccount(accountName);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }



    @NotNull
    private ProviderAccountModel serializeToModel(String textModel) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(textModel, ProviderAccountModel.class);
        }
        catch (IOException ex) {
            LoggerFactory.getLogger(ProviderAccountController.class).error("Error when serializing {} to ProviderAccountModel: \n" +
                    "{}", textModel, Arrays.toString(ex.getStackTrace()));
            throw new SerializationException("Serialization Failed due to IOException", ex);
        }
    }


}
