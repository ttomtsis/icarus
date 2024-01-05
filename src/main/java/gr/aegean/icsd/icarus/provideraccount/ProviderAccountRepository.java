package gr.aegean.icsd.icarus.provideraccount;

import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderAccountRepository extends JpaRepository<ProviderAccount, Long> {

    void deleteByNameAndCreator(String accountName, IcarusUser creator);

    Optional<ProviderAccount> findByNameAndCreator(String accountName, IcarusUser creator);

}
