package gr.aegean.icsd.icarus.provideraccount;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import gr.aegean.icsd.icarus.icarususer.IcarusUserRepository;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidEntityConfigurationException;
import gr.aegean.icsd.icarus.util.security.UserUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                .orElseThrow(() -> new EntityNotFoundException(IcarusUser.class, username));

        // Convert the Set to a List
        List<ProviderAccount> accounts = new ArrayList<>(user.getAccounts());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), accounts.size());

        return new PageImpl<>(accounts.subList(start, end), pageable, accounts.size());
    }


    public AwsAccount attachAwsAccount(@NotBlank String username, @NotNull ProviderAccount account) {

        IcarusUser icarusUser = checkIfUserExists(username);

        ProviderAccount savedAccount = accountRepository.save(account);

        icarusUser.addAccount(account);
        userRepository.saveAndFlush(icarusUser);

        return (AwsAccount) savedAccount;
    }


    public GcpAccount attachGcpAccount(@NotBlank String username, @NotNull GcpAccount account,
                                       @NotNull MultipartFile gcpKeyfile) {

        IcarusUser icarusUser = checkIfUserExists(username);

        String keyFile = convertMultipartFileToString(gcpKeyfile);
        isValidJson(keyFile);

        account.setGcpKeyfile(keyFile);
        GcpAccount savedAccount = accountRepository.save(account);

        icarusUser.addAccount(account);
        userRepository.saveAndFlush(icarusUser);

        return savedAccount;
    }


    public void updateAwsAccount(@NotBlank String awsAccountName, @NotNull AwsAccount updatedAwsAccount) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        Optional<ProviderAccount> existingProviderAccount = accountRepository.findByNameAndCreator
                (awsAccountName, loggedInUser);

        if (existingProviderAccount.isEmpty()) {throw new EntityNotFoundException(AwsAccount.class, awsAccountName);}

        AwsAccount existingAwsAccount = (AwsAccount) existingProviderAccount.get();

        String updatedAwsAccessKey = updatedAwsAccount.getAwsAccessKey();
        String updatedAwsSecretKey = updatedAwsAccount.getAwsSecretKey();

        if (!StringUtils.isBlank(updatedAwsAccessKey) && !StringUtils.isBlank(updatedAwsSecretKey)) {
            existingAwsAccount.changeCredentials(updatedAwsAccessKey, updatedAwsSecretKey);
        }

        String updatedAwsAccountDescription = updatedAwsAccount.getDescription();
        if (updatedAwsAccountDescription != null) {
            existingAwsAccount.setDescription(updatedAwsAccountDescription);
        }

        accountRepository.save(existingAwsAccount);
    }


    public void updateGcpAccount(@NotBlank String gcpAccountName, GcpAccount updatedGcpAccount,
                                      MultipartFile updatedGcpKeyfile) {

        if (updatedGcpAccount == null && updatedGcpKeyfile == null) {
            throw new IllegalArgumentException("Unable to complete operation, no keyfile or metadata have been provided");
        }

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        Optional<ProviderAccount> existingProviderAccount = accountRepository.findByNameAndCreator
                (gcpAccountName, loggedInUser);

        // GCP account exists
        if (existingProviderAccount.isEmpty()) {throw new EntityNotFoundException(GcpAccount.class, gcpAccountName);}

        GcpAccount existingGcpAccount = (GcpAccount) existingProviderAccount.get();

        // New keyfile provided
        if (updatedGcpKeyfile != null) {

            String updatedKeyfile = convertMultipartFileToString(updatedGcpKeyfile);
            isValidJson(updatedKeyfile);

            existingGcpAccount.setGcpKeyfile(updatedKeyfile);
        }

        // New metadata provided
        if (updatedGcpAccount != null) {

            String updatedGcpAccountName = updatedGcpAccount.getName();
            if (StringUtils.isNotBlank(updatedGcpAccountName)) {
                existingGcpAccount.setName(updatedGcpAccountName);
            }

            String updatedGcpAccountDescription = updatedGcpAccount.getDescription();
            if (updatedGcpAccountDescription != null) {
                existingGcpAccount.setDescription(updatedGcpAccountDescription);
            }

            if (!StringUtils.isBlank(updatedGcpAccount.getGcpProjectId())) {
                existingGcpAccount.setGcpProjectId(updatedGcpAccount.getGcpProjectId());
            }
        }


        accountRepository.save(existingGcpAccount);
    }


    public void detachProviderAccount(@NotBlank String accountName) {

        IcarusUser loggedInUser = UserUtils.getLoggedInUser();
        accountRepository.deleteByNameAndCreator(accountName, loggedInUser);
    }



    private IcarusUser checkIfUserExists(String username) {

        return userRepository.findUserByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(IcarusUser.class, username)
        );
    }

    private String convertMultipartFileToString(MultipartFile file) {

        try {
            byte[] bytes = file.getBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to String", e);
        }

    }

    private void isValidJson(String jsonInString) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);

        } catch (Exception e) {
            throw new InvalidEntityConfigurationException(ProviderAccount.class,
                    "The provided GCP keyfile is not a valid JSON");
        }

    }


}
