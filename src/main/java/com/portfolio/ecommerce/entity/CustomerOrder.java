package com.portfolio.ecommerce.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(indexes = @Index(name = "idx_customer_order_customer_created", columnList = "customer_id, created_at DESC"))
public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Customer customer;
 public Instant createdAt=Instant.now();
 @Column(precision=12,scale=2) public BigDecimal total=BigDecimal.ZERO;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) public List<OrderLine> lines=new ArrayList<>();
}
