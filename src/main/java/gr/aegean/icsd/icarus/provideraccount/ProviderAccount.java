package gr.aegean.icsd.icarus.provideraccount;

import gr.aegean.icsd.icarus.test.Test;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "providerAccount")
public class ProviderAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Column(unique = true)
    @Size(min = minLength, max = maxLength, message = "Name does not conform to length limitations")
    private String name;

    @Size(min = minLength, max = maxDescriptionLength, message = "Description does not conform to length limitations")
    private String description;

    @ManyToMany(mappedBy = "accountsList", cascade = {CascadeType.REFRESH, CascadeType.REMOVE},
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


}