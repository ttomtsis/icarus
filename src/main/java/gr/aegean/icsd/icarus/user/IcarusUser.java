package gr.aegean.icsd.icarus.user;

import gr.aegean.icsd.icarus.account.ProviderAccount;
import gr.aegean.icsd.icarus.test.Test;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jdk.jfr.BooleanFlag;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.maxLength;
import static gr.aegean.icsd.icarus.util.constants.IcarusConstants.minLength;


@Entity
@Table(name = "icarusUser")
public class IcarusUser implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Column(unique = true)
    @Size(min = minLength, max = maxLength, message = "Username does not conform to length limitations")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false, name = "password")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email
    private String email;


    @OneToMany(targetEntity = ProviderAccount.class, orphanRemoval = true,
            cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private final Set<ProviderAccount> accounts = new HashSet<>();

    @OneToMany(mappedBy = "testAuthor", targetEntity = Test.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    private final Set<Test> createdTests = new HashSet<>();

    @BooleanFlag
    @NotNull
    @Column(nullable = false, name = "enabled")
    private boolean accountEnabled;

    @BooleanFlag
    @NotNull
    @Column(name = "account_non_expired", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean accountNonExpired;

    @BooleanFlag
    @NotNull
    @Column(name = "account_non_locked", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean accountNonLocked;

    @BooleanFlag
    @NotNull
    @Column(name = "credentials_non_expired", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean credentialsNonExpired;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "authorities", joinColumns = @JoinColumn(name = "username"))
    @Column(name = "authority")
    private final Set<GrantedAuthority> authorities = new HashSet<>();



    public IcarusUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;

        this.accountEnabled = true;
        this.accountNonLocked = true;
        this.accountNonExpired = true;
        this.credentialsNonExpired = true;
    }

    public IcarusUser() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public Set<ProviderAccount> getAccounts() {
        return accounts;
    }

    public void addAccount(ProviderAccount newAccount) {
        this.accounts.add(newAccount);
    }

    public void removeAccount(long accountID) {
        accounts.removeIf(account -> account.getId().equals(accountID));
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.accountEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public Set<Test> getCreatedTests() {return this.createdTests;}

}
