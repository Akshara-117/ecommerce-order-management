package com.portfolio.ecommerce.service;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service public class OrderService {
 private final CustomerRepository customers; private final ProductRepository products; private final CartItemRepository carts; private final CustomerOrderRepository orders;
 public OrderService(CustomerRepository customers,ProductRepository products,CartItemRepository carts,CustomerOrderRepository orders){this.customers=customers;this.products=products;this.carts=carts;this.orders=orders;}
 @Transactional public CartItem addToCart(Long customerId,Long productId,int quantity){
  if(quantity<1)throw new IllegalArgumentException("Quantity must be positive");
  Customer c=customers.findById(customerId).orElseThrow(()->new IllegalArgumentException("Customer not found"));
  Product p=products.findById(productId).orElseThrow(()->new IllegalArgumentException("Product not found"));
  CartItem item=carts.findByCustomerIdAndProductId(customerId,productId).orElseGet(()->new CartItem(c,p,0));
  item.quantity=Math.addExact(item.quantity,quantity);
  if(item.quantity>p.stock)throw new IllegalArgumentException("Not enough inventory");
  return carts.save(item);
 }
 @Transactional public CustomerOrder checkout(Long customerId){
  Customer c=customers.findById(customerId).orElseThrow(()->new IllegalArgumentException("Customer not found"));
  List<CartItem> items=carts.findByCustomerId(customerId);
  if(items.isEmpty())throw new IllegalArgumentException("Cart is empty");
  CustomerOrder order=new CustomerOrder();order.customer=c;
  for(CartItem item:items){
   Product p=products.findById(item.product.id).orElseThrow(()->new IllegalArgumentException("Product not found"));
   if(item.quantity<1||p.stock<item.quantity)throw new IllegalArgumentException("Insufficient inventory for "+p.name);
   p.stock-=item.quantity;
   OrderLine line=new OrderLine();line.order=order;line.productId=p.id;line.productName=p.name;line.quantity=item.quantity;line.unitPrice=p.price;
   order.lines.add(line);order.total=order.total.add(p.price.multiply(BigDecimal.valueOf(item.quantity)));
  }
  CustomerOrder saved=orders.saveAndFlush(order);
  carts.deleteByCustomerId(customerId);
  return saved;
 }
}
