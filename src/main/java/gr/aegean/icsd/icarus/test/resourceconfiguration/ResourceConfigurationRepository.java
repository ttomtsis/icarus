package gr.aegean.icsd.icarus.test.resourceconfiguration;

import gr.aegean.icsd.icarus.test.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ResourceConfigurationRepository extends JpaRepository<ResourceConfiguration, Long> {

    Page<ResourceConfiguration> findAllByParentTest(Test parentTest, Pageable pageable);

}
