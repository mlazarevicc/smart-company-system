package ftn.siit.nvt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer", indexes = {
        @Index(name = "idx_customer_email", columnList = "email", unique = true),
        @Index(name = "idx_customer_username", columnList = "username", unique = true)
})
public class Customer extends User {
}
