package com.portfolio.ecommerce.service;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
class OrderServiceTest {
 @Test void checkoutRejectsEmptyCart(){
  CustomerRepository customers=mock(CustomerRepository.class);ProductRepository products=mock(ProductRepository.class);CartItemRepository carts=mock(CartItemRepository.class);CustomerOrderRepository orders=mock(CustomerOrderRepository.class);
  Customer c=new Customer("Test","test@example.com");c.id=1L;
  when(customers.findById(1L)).thenReturn(Optional.of(c));when(carts.findByCustomerId(1L)).thenReturn(List.of());
  OrderService service=new OrderService(customers,products,carts,orders);
  assertThrows(IllegalArgumentException.class,()->service.checkout(1L));
  verify(orders,never()).saveAndFlush(any());
 }
 @Test void addToCartRejectsInsufficientStock(){
  CustomerRepository customers=mock(CustomerRepository.class);ProductRepository products=mock(ProductRepository.class);CartItemRepository carts=mock(CartItemRepository.class);CustomerOrderRepository orders=mock(CustomerOrderRepository.class);
  Customer c=new Customer("Test","test@example.com");c.id=1L;
  Product p=new Product("Mouse",new BigDecimal("20.00"),2);p.id=5L;
  when(customers.findById(1L)).thenReturn(Optional.of(c));when(products.findById(5L)).thenReturn(Optional.of(p));when(carts.findByCustomerIdAndProductId(1L,5L)).thenReturn(Optional.empty());
  assertThrows(IllegalArgumentException.class,()->new OrderService(customers,products,carts,orders).addToCart(1L,5L,3));
  verify(carts,never()).save(any());
 }
}
