package gr.aegean.icsd.icarus.icarususer;

import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.test.Test;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class IcarusUserModelAssembler extends RepresentationModelAssemblerSupport<IcarusUser, IcarusUserModel> {


    public IcarusUserModelAssembler() {
        super(IcarusUserController.class, IcarusUserModel.class);
    }



    @Override
    public IcarusUserModel toModel(IcarusUser entity) {

        Set<Long> accounts = new HashSet<>();
        for (ProviderAccount account : entity.getAccounts()) {

            accounts.add(account.getId());
        }

        Set<Long> createdTests = new HashSet<>();
        for (Test test : entity.getCreatedTests()) {

            createdTests.add(test.getId());
        }

        Set<String> authorities = new HashSet<>();
        for (GrantedAuthority authority : entity.getAuthorities()) {

            authorities.add(authority.getAuthority());
        }


        IcarusUserModel newModel = new IcarusUserModel();

        newModel.setId(entity.getId());
        newModel.setUsername(entity.getUsername());
        newModel.setPassword(entity.getPassword());
        newModel.setEmail(entity.getEmail());

        String formattedCredentialsLastChanged = DateTimeFormatter.ofPattern("MMM dd yyyy h:mm:ss a")
                .withZone(ZoneId.systemDefault())
                .format(entity.getCredentialsLastChanged());

        newModel.setCredentialsLastChanged(formattedCredentialsLastChanged);

        newModel.setAccounts(accounts);
        newModel.setCreatedTest(createdTests);
        newModel.setAuthorities(authorities);
        newModel.setAccountNonExpired(entity.isAccountNonExpired());
        newModel.setAccountNonLocked(entity.isAccountNonLocked());
        newModel.setCredentialsNonExpired(entity.isCredentialsNonExpired());

        return addLinksToModel(newModel);
    }


    private IcarusUserModel addLinksToModel(IcarusUserModel model) {

        model.add(linkTo(methodOn(IcarusUserController.class)
                .viewAccount()).withSelfRel());

        model.add(linkTo(methodOn(IcarusUserController.class)
                .deleteAccount()).withRel("Delete Account"));

        model.add(linkTo(methodOn(IcarusUserController.class)
                .updateAccount(new IcarusUserModel())).withRel("Update Account"));


        return model;
    }


}
