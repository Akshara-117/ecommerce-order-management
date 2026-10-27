package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.*;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import com.portfolio.ecommerce.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    // Opt-in paginated endpoint; the existing /orders response remains backward compatible.
    @GetMapping("/customers/{id}/orders/paged")
    public Page<Map<String, Object>> pagedOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "page must be >= 0 and size must be between 1 and 100");
        }
        PageRequest request = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return orders.findByCustomer_Id(id, request)
                .map(o -> Map.<String, Object>of(
                        "orderId", o.id,
                        "total", o.total,
                        "createdAt", o.createdAt));
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
