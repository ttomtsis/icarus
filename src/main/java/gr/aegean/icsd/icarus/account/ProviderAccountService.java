package gr.aegean.icsd.icarus.account;

import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.user.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.exceptions.ProviderAccountNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.UserNotFoundException;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;


@Service
@Transactional
@Validated
public class ProviderAccountService {

    private final IcarusUserRepository userRepository;
    private final ProviderAccountRepository accountRepository;


    public ProviderAccountService(IcarusUserRepository repository,
                                  ProviderAccountRepository accountRepository) {
        this.userRepository = repository;
        this.accountRepository = accountRepository;
    }


    public ProviderAccount attachProviderAccount(@NotBlank String username, @NotNull ProviderAccount account) {

        Optional<IcarusUser> icarusUser = userRepository.findUserByUsername(username);

        icarusUser.ifPresentOrElse(

                user -> {
                    user.addAccount(account);
                    userRepository.saveAndFlush(user);
                    },

                () -> {
                    throw new UserNotFoundException("User: " + username + ", not found");
                }
        );

        return account;
    }

    public void updateProviderAccount(@NotBlank String awsAccountName, @NotNull AwsAccount updatedAwsAccount) {

        Optional<ProviderAccount> existingProviderAccount = accountRepository.findByName(awsAccountName);

        if (existingProviderAccount.isEmpty()) {throw new ProviderAccountNotFoundException("Account: " +
                awsAccountName + ", was not found");}

        AwsAccount existingAwsAccount = (AwsAccount) existingProviderAccount.get();

        String updatedAwsAccessKey = updatedAwsAccount.getAwsAccessKey();
        String updatedAwsSecretKey = updatedAwsAccount.getAwsSecretKey();

        if (!StringUtils.isBlank(updatedAwsAccessKey) && !StringUtils.isBlank(updatedAwsSecretKey)) {
            existingAwsAccount.changeCredentials(updatedAwsAccessKey, updatedAwsSecretKey);
        }

        String updatedAwsAccountDescription = updatedAwsAccount.getDescription();
        if (!StringUtils.isBlank(updatedAwsAccountDescription)) {
            existingAwsAccount.setDescription(updatedAwsAccountDescription);
        }

        accountRepository.save(existingAwsAccount);
    }

    public void updateProviderAccount(@NotBlank String gcpAccountName, @NotNull GcpAccount updatedGcpAccount) {

        Optional<ProviderAccount> existingProviderAccount = accountRepository.findByName(gcpAccountName);

        if (existingProviderAccount.isEmpty()) {throw new ProviderAccountNotFoundException("Account: " +
                gcpAccountName + ", was not found");}

        GcpAccount existingGcpAccount = (GcpAccount) existingProviderAccount.get();

        String updatedGcpKeyfile = updatedGcpAccount.getGcpKeyfile();

        if (!StringUtils.isBlank(updatedGcpKeyfile) && !updatedGcpKeyfile.equals("null")) {
            existingGcpAccount.setGcpKeyfile(updatedGcpKeyfile);
            LoggerFactory.getLogger("bob").warn("Updating keyfile");
        }

        String updatedGcpAccountDescription = updatedGcpAccount.getDescription();

        if (!StringUtils.isBlank(updatedGcpAccountDescription)) {
            existingGcpAccount.setDescription(updatedGcpAccountDescription);
        }

        accountRepository.save(existingGcpAccount);
    }

    public void detachProviderAccount(@NotBlank String accountName) {
        accountRepository.deleteByName(accountName);
    }


}
