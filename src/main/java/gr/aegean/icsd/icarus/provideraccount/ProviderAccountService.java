package gr.aegean.icsd.icarus.provideraccount;

import gr.aegean.icsd.icarus.user.IcarusUser;
import gr.aegean.icsd.icarus.user.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.exceptions.UserNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.provideraccount.ProviderAccountNotFoundException;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
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



    public Page<ProviderAccount> getAccounts(String username, Pageable pageable) {

        IcarusUser user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        // Convert the Set to a List
        List<ProviderAccount> accounts = new ArrayList<>(user.getAccounts());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), accounts.size());

        return new PageImpl<>(accounts.subList(start, end), pageable, accounts.size());
    }

    public ProviderAccount attachProviderAccount(@NotBlank String username, @NotNull ProviderAccount account) {

        IcarusUser icarusUser = checkIfUserExists(username);

        ProviderAccount savedAccount = accountRepository.save(account);

        icarusUser.addAccount(account);
        userRepository.saveAndFlush(icarusUser);

        return savedAccount;
    }

    public void updateProviderAccount(@NotBlank String awsAccountName, @NotNull AwsAccount updatedAwsAccount) {

        Optional<ProviderAccount> existingProviderAccount = accountRepository.findByName(awsAccountName);

        if (existingProviderAccount.isEmpty()) {throw new ProviderAccountNotFoundException(awsAccountName);}

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

        if (existingProviderAccount.isEmpty()) {throw new ProviderAccountNotFoundException(gcpAccountName);}

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

        if (!StringUtils.isBlank(updatedGcpAccount.getGcpProjectId())) {
            existingGcpAccount.setGcpProjectId(updatedGcpAccount.getGcpProjectId());
        }

        accountRepository.save(existingGcpAccount);
    }

    public void detachProviderAccount(@NotBlank String accountName) {
        accountRepository.deleteByName(accountName);
    }

    private IcarusUser checkIfUserExists(String username) {

        return userRepository.findUserByUsername(username).orElseThrow(
                () -> new UserNotFoundException(username)
        );
    }
}
