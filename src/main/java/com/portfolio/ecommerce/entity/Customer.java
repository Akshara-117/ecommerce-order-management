package com.portfolio.ecommerce.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity public class Customer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String name;
 @Email @NotBlank @Column(unique=true) public String email;
 public Customer(){} public Customer(String name,String email){this.name=name;this.email=email;}
}
