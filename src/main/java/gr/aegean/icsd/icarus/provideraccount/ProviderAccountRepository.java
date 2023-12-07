package gr.aegean.icsd.icarus.provideraccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderAccountRepository extends JpaRepository<ProviderAccount, Long> {

    void deleteByName(String accountName);

    Optional<ProviderAccount> findByName(String accountName);

}
