package gr.aegean.icsd.icarus.user;

import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import gr.aegean.icsd.icarus.util.security.httpbasic.SqlAuthenticationManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;


@Service
@Transactional
@Validated
public class IcarusUserService {


    private final IcarusUserRepository repository;
    private final SqlAuthenticationManager authenticationManager;

    @Value("${security.users.functionSourcesDirectory}")
    private String functionSourcesDirectory;



    public IcarusUserService(IcarusUserRepository repository,
                             SqlAuthenticationManager authenticationManager) {

        this.repository = repository;
        this.authenticationManager = authenticationManager;
    }



    public void deleteUserAccount() {

        String loggedInUserUsername = UserUtils.getUsername();

        authenticationManager.deleteUser(loggedInUserUsername);

        String usersFunctionDirectory = functionSourcesDirectory + "\\Functions\\" + loggedInUserUsername;
        deleteDirectory(usersFunctionDirectory);
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


    private void deleteDirectory(String dir) {

        try {

            Files.walkFileTree(Path.of(dir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        }
        catch (IOException ex) {
            LoggerFactory.getLogger(IcarusUserService.class).error("Could not delete directory: {}\n{}", dir,
                    Arrays.toString(ex.getStackTrace()));
        }
    }


}
