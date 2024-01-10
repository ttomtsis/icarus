package gr.aegean.icsd.icarus.report;

import gr.aegean.icsd.icarus.testexecution.TestExecution;
import gr.aegean.icsd.icarus.user.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @OneToOne(targetEntity = TestExecution.class)
    @JoinColumn(name = "associated_execution")
    private TestExecution associatedExecution;

    @Lob
    private byte[] reportDocument;

    @NotBlank
    private String documentName;



    public Report(TestExecution associatedExecution, byte[] reportDocument, String documentName) {

        this.creator = associatedExecution.getCreator();
        this.associatedExecution = associatedExecution;
        this.reportDocument = reportDocument;
        this.documentName = documentName;
    }

    public Report() {}



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

    public byte[] getReportDocument() {
        return reportDocument;
    }

    public String getDocumentName() {
        return documentName;
    }


}
