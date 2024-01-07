package gr.aegean.icsd.icarus.user;

import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.security.httpbasic.SqlAuthenticationManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@Transactional
@Validated
public class IcarusUserService {


    private final IcarusUserRepository repository;
    private final SqlAuthenticationManager authenticationManager;



    public IcarusUserService(IcarusUserRepository repository,
                             SqlAuthenticationManager authenticationManager) {

        this.repository = repository;
        this.authenticationManager = authenticationManager;
    }



    public void deleteUserAccount() {

        String loggedInUserUsername = UserUtils.getUsername();
        authenticationManager.deleteUser(loggedInUserUsername);
    }

    public void updateUserAccount(@NotNull IcarusUser updatedUser) {

        authenticationManager.updateUser(updatedUser);
    }

    public IcarusUser createUserAccount(@NotNull IcarusUser newUser) {

        return authenticationManager.createIcarusUser(newUser);
    }

    public IcarusUser viewUserAccount() {

        return repository.findById(UserUtils.getLoggedInUser().getId())
                .orElseThrow(() -> new EntityNotFoundException(IcarusUser.class, UserUtils.getUsername()));
    }

    public void resetAccountPassword(@NotBlank String icarusUserEmail) {

        throw new UnsupportedOperationException("Password reset has not been implemented as of yet");
    }


}
