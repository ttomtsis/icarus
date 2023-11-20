package gr.aegean.icsd.icarus.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderAccountRepository extends JpaRepository<ProviderAccount, Long> {



}
