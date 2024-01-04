package gr.aegean.icsd.icarus.report;

import gr.aegean.icsd.icarus.user.IcarusUser;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Report {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser creator;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}
