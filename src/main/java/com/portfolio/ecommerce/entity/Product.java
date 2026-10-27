package com.portfolio.ecommerce.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
@Entity public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String name;
 @NotNull @DecimalMin("0.0") @Column(precision=12,scale=2) public BigDecimal price;
 @Min(0) public int stock;
 @Version public Long version;
 public Product(){} public Product(String name,BigDecimal price,int stock){this.name=name;this.price=price;this.stock=stock;}
}
