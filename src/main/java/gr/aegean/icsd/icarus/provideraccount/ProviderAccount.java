package gr.aegean.icsd.icarus.provideraccount;

import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.icarususer.IcarusUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "provider_account")
@EntityListeners(AuditingEntityListener.class)
public class ProviderAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(updatable = false)
    private IcarusUser creator;

    @NotBlank(message = "Name cannot be blank")
    @Column(unique = true)
    @Size(min = MIN_LENGTH, max = MAX_LENGTH, message = "Name does not conform to length limitations")
    private String name;

    @Size(min = MIN_LENGTH, max = MAX_DESCRIPTION_LENGTH, message = "Description does not conform to length limitations")
    private String description;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String accountType;

    @ManyToMany(mappedBy = "accountsList", cascade = CascadeType.REFRESH,
            targetEntity = Test.class)
    private final Set<Test> associatedTests = new HashSet<>();



    public ProviderAccount(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ProviderAccount(String name) {
        this.name = name;
    }

    public ProviderAccount() {}



    @PreRemove
    private void removeForeignKeyConstraints() {

        for (Test associatedTest : this.associatedTests) {
            associatedTest.removeAccount(this);
        }
    }


    // GETTERS - SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Test> getAssociatedTests() {
        return associatedTests;
    }

    public String getAccountType() {
        return accountType;
    }

    public IcarusUser getCreator() {
        return creator;
    }

    public void setCreator(IcarusUser creator) {
        this.creator = creator;
    }


}
