package gr.aegean.icsd.icarus.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ResourceConfigurationRepository extends JpaRepository<ResourceConfiguration, Long> {

    Optional<ResourceConfiguration> findResourceConfigurationByIdAndCreator(Long id, IcarusUser creator);
    Page<ResourceConfiguration> findAllByParentTestAndCreator(Test parentTest, IcarusUser creator, Pageable pageable);

}
