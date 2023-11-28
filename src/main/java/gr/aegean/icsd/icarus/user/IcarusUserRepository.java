package gr.aegean.icsd.icarus.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IcarusUserRepository extends JpaRepository<IcarusUser, Long> {

    Optional<IcarusUser> findUserByUsername(String username);

}
