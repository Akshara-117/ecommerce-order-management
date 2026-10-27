package com.portfolio.ecommerce.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity public class OrderLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public CustomerOrder order;
 public Long productId;
 public String productName;
 public int quantity;
 @Column(precision=12,scale=2) public BigDecimal unitPrice;
}
