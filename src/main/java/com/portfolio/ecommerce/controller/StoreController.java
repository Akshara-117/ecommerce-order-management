package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.*;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import com.portfolio.ecommerce.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StoreController {
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final CartItemRepository carts;
    private final CustomerOrderRepository orders;
    private final OrderService service;

    public StoreController(ProductRepository products, CustomerRepository customers,
                           CartItemRepository carts, CustomerOrderRepository orders,
                           OrderService service) {
        this.products = products;
        this.customers = customers;
        this.carts = carts;
        this.orders = orders;
        this.service = service;
    }

    @GetMapping("/products")
    public List<Product> products() { return products.findAll(); }

    @PostMapping("/products")
    public Product createProduct(@Valid @RequestBody Product p) {
        p.id = null;
        p.version = null;
        return products.save(p);
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable Long id) {
        return products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product p) {
        Product current = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        current.name = p.name;
        current.price = p.price;
        current.stock = p.stock;
        return products.save(current);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id) { products.deleteById(id); }

    @PostMapping("/customers")
    public Customer createCustomer(@Valid @RequestBody Customer c) {
        c.id = null;
        return customers.save(c);
    }

    @GetMapping("/customers/{id}/cart")
    public List<CartItem> cart(@PathVariable Long id) { return carts.findByCustomerId(id); }

    @PostMapping("/customers/{id}/cart")
    public CartItem addCart(@PathVariable Long id, @Valid @RequestBody AddCartItemRequest request) {
        return service.addToCart(id, request.productId(), request.quantity());
    }

    @PostMapping("/customers/{id}/checkout")
    public Map<String, Object> checkout(@PathVariable Long id) {
        CustomerOrder order = service.checkout(id);
        return Map.of("orderId", order.id, "total", order.total, "items", order.lines.size());
    }

    @GetMapping("/customers/{id}/orders")
    public List<Map<String, Object>> orders(@PathVariable Long id) {
        // Database-side filtering prevents fetching every customer's orders.
        return orders.findByCustomer_IdOrderByCreatedAtDesc(id).stream()
                .map(o -> Map.<String, Object>of(
                        "orderId", o.id,
                        "total", o.total,
                        "createdAt", o.createdAt))
                .toList();
    }
}
