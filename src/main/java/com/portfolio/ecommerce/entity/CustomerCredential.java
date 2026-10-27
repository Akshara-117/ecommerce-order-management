package com.portfolio.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class CustomerCredential {
    @Id public Long customerId;
    @OneToOne(optional = false) @MapsId @JoinColumn(name = "customer_id")
    public Customer customer;
    @Column(nullable = false, length = 100)
    public String passwordHash;

    protected CustomerCredential() {}
    public CustomerCredential(Customer customer, String passwordHash) {
        this.customer = customer;
        this.passwordHash = passwordHash;
    }
}
