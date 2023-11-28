package gr.aegean.icsd.icarus.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = "application/json", value = "api/v0/users/{id}/accounts")
public class ProviderAccountController {

    @PostMapping(value = "/aws", consumes = "application/json")
    public ResponseEntity<ProviderAccountModel> attachAwsAccount() {

        return null;
    }

}
