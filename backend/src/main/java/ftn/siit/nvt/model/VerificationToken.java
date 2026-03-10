package ftn.siit.nvt.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class VerificationToken {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(targetEntity = Customer.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public VerificationToken(Customer customer) {
        this.customer = customer;
        this.expiryDate = calculateExpiryDate();
        this.token = UUID.randomUUID().toString();
    }

    public VerificationToken() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(VerificationToken.EXPIRATION_MINUTES);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}