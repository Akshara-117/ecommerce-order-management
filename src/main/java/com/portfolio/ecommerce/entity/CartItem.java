package com.portfolio.ecommerce.entity;
import jakarta.persistence.*;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"customer_id","product_id"})) public class CartItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Customer customer;
 @ManyToOne(optional=false) public Product product;
 public int quantity;
 public CartItem(){} public CartItem(Customer c,Product p,int q){customer=c;product=p;quantity=q;}
}
