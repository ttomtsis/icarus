package gr.aegean.icsd.icarus.icarususer;

import org.springframework.hateoas.RepresentationModel;

import java.util.Set;


public class IcarusUserModel extends RepresentationModel<IcarusUserModel> {


    private Long id;
    private String username;
    private String password;
    private String email;
    private String credentialsLastChanged;
    private Set<Long> accounts;
    private Set<Long> createdTest;
    private Set<String> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;



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

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Long> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Long> accounts) {
        this.accounts = accounts;
    }

    public Set<Long> getCreatedTest() {
        return createdTest;
    }

    public void setCreatedTest(Set<Long> createdTest) {
        this.createdTest = createdTest;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getCredentialsLastChanged() {
        return credentialsLastChanged;
    }

    public void setCredentialsLastChanged(String credentialsLastChanged) {
        this.credentialsLastChanged = credentialsLastChanged;
    }


}
