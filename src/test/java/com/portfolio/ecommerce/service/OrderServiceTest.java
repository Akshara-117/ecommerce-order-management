
package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private CustomerRepository customers;
    private ProductRepository products;
    private CartItemRepository carts;
    private CustomerOrderRepository orders;
    private OrderService service;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        // Mock database repositories so tests do not need MySQL.
        customers = mock(CustomerRepository.class);
        products = mock(ProductRepository.class);
        carts = mock(CartItemRepository.class);
        orders = mock(CustomerOrderRepository.class);

        service = new OrderService(
                customers, products, carts, orders
        );

        // Reusable test customer.
        customer = new Customer("Test", "test@example.com");
        customer.id = 1L;

        // Reusable product with 10 units in stock.
        product = new Product(
                "Laptop",
                new BigDecimal("999.99"),
                10
        );
        product.id = 5L;
    }

    @Test
    void checkoutRejectsEmptyCart() {
        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(carts.findByCustomerId(1L))
                .thenReturn(List.of());

        // Checkout must fail if the cart contains no items.
        assertThrows(
                IllegalArgumentException.class,
                () -> service.checkout(1L)
        );

        // No order should be saved.
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void addToCartRejectsInsufficientStock() {
        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(products.findById(5L))
                .thenReturn(Optional.of(product));

        when(carts.findByCustomerIdAndProductId(1L, 5L))
                .thenReturn(Optional.empty());

        // Attempt to add 11 units when only 10 exist.
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addToCart(1L, 5L, 11)
        );

        verify(carts, never()).save(any());
    }

    @Test
    void addToCartRejectsZeroQuantity() {
        // Quantity must always be positive.
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addToCart(1L, 5L, 0)
        );

        verify(carts, never()).save(any());
    }

    @Test
    void addToCartRejectsNegativeQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addToCart(1L, 5L, -2)
        );

        verify(carts, never()).save(any());
    }

    @Test
    void addToCartRejectsMissingCustomer() {
        when(customers.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addToCart(99L, 5L, 2)
        );

        verify(carts, never()).save(any());
    }

    @Test
    void addToCartRejectsMissingProduct() {
        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(products.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addToCart(1L, 99L, 2)
        );

        verify(carts, never()).save(any());
    }

    @Test
    void addToCartSuccessfullySavesItem() {
        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(products.findById(5L))
                .thenReturn(Optional.of(product));

        when(carts.findByCustomerIdAndProductId(1L, 5L))
                .thenReturn(Optional.empty());

        // Return the item supplied to the mocked repository.
        when(carts.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = service.addToCart(1L, 5L, 2);

        assertEquals(2, result.quantity);
        assertEquals(5L, result.product.id);

        verify(carts).save(any(CartItem.class));
    }

    @Test
    void checkoutRejectsInsufficientInventory() {
        CartItem item = new CartItem(customer, product, 11);

        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(carts.findByCustomerId(1L))
                .thenReturn(List.of(item));

        when(products.findById(5L))
                .thenReturn(Optional.of(product));

        // Stock is only 10, so checkout must fail.
        assertThrows(
                IllegalArgumentException.class,
                () -> service.checkout(1L)
        );

        assertEquals(10, product.stock);

        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void checkoutSuccessfullyCreatesOrderAndUpdatesInventory() {
        CartItem item = new CartItem(customer, product, 2);

        when(customers.findById(1L))
                .thenReturn(Optional.of(customer));

        when(carts.findByCustomerId(1L))
                .thenReturn(List.of(item));

        when(products.findById(5L))
                .thenReturn(Optional.of(product));

        // Simulate the database returning the saved order.
        when(orders.saveAndFlush(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = service.checkout(1L);

        // 2 laptops x 999.99 = 1999.98.
        assertEquals(
                0,
                new BigDecimal("1999.98").compareTo(result.total)
        );

        // Inventory should decrease from 10 to 8.
        assertEquals(8, product.stock);

        // One order line contains two laptops.
        assertEquals(1, result.lines.size());
        assertEquals(2, result.lines.get(0).quantity);

        // Confirm the order was saved and cart cleared.
        verify(orders).saveAndFlush(any(CustomerOrder.class));
        verify(carts).deleteByCustomerId(1L);
    }

    @Test
    void checkoutRejectsMissingCustomer() {
        when(customers.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.checkout(99L)
        );

        verify(orders, never()).saveAndFlush(any());
    }
}
