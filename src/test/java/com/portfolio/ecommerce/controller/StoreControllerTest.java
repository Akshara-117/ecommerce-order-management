package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.entity.Customer;
import com.portfolio.ecommerce.entity.CustomerOrder;
import com.portfolio.ecommerce.repository.CartItemRepository;
import com.portfolio.ecommerce.repository.CustomerOrderRepository;
import com.portfolio.ecommerce.repository.CustomerRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import com.portfolio.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreControllerTest {

    @Test
    void orderHistoryQueriesOnlyRequestedCustomerAndPreservesResponseShape() {
        ProductRepository products = mock(ProductRepository.class);
        CustomerRepository customers = mock(CustomerRepository.class);
        CartItemRepository carts = mock(CartItemRepository.class);
        CustomerOrderRepository orders = mock(CustomerOrderRepository.class);
        OrderService service = mock(OrderService.class);
        StoreController controller = new StoreController(products, customers, carts, orders, service);

        CustomerOrder order = new CustomerOrder();
        order.id = 42L;
        order.customer = new Customer("Test", "test@example.com");
        order.total = new BigDecimal("1999.98");
        order.createdAt = Instant.parse("2026-10-27T17:52:00Z");

        when(orders.findByCustomer_IdOrderByCreatedAtDesc(7L)).thenReturn(List.of(order));

        List<Map<String, Object>> result = controller.orders(7L);

        assertEquals(1, result.size());
        assertEquals(42L, result.get(0).get("orderId"));
        assertEquals(new BigDecimal("1999.98"), result.get(0).get("total"));
        assertEquals(order.createdAt, result.get(0).get("createdAt"));
        verify(orders).findByCustomer_IdOrderByCreatedAtDesc(7L);
        verify(orders, never()).findAll();
    }

    @Test
    void orderHistoryReturnsEmptyListWhenCustomerHasNoOrders() {
        CustomerOrderRepository orders = mock(CustomerOrderRepository.class);
        StoreController controller = new StoreController(
                mock(ProductRepository.class), mock(CustomerRepository.class),
                mock(CartItemRepository.class), orders, mock(OrderService.class));

        when(orders.findByCustomer_IdOrderByCreatedAtDesc(9L)).thenReturn(List.of());

        assertTrue(controller.orders(9L).isEmpty());
        verify(orders, never()).findAll();
    }
}
