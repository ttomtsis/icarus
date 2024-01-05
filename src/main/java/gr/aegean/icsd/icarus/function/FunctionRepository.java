package gr.aegean.icsd.icarus.function;

import gr.aegean.icsd.icarus.user.IcarusUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {

    Optional<Function> findFunctionByIdAndAuthor(Long id, IcarusUser author);

}
