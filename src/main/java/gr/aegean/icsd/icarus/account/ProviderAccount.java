package gr.aegean.icsd.icarus.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static gr.aegean.icsd.icarus.util.IcarusConstants.*;

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

}
