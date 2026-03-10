package ftn.siit.nvt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class Manager extends User {
    @Column(nullable = false)
    private Boolean isBlocked = false;
    @Column(nullable = false)
    private Boolean isSupermanager = false;
    @Column(nullable = false)
    private Boolean resetPassword = false;

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    public Manager() {
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public Boolean getSupermanager() {
        return isSupermanager;
    }

    public void setSupermanager(Boolean supermanager) {
        isSupermanager = supermanager;
    }

    public Boolean getResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(Boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + getRole().name())); // ROLE_MANAGER

        if (isSupermanager) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPERMANAGER"));
        }
        return authorities;
    }
}
