package com.portfolio.ecommerce.repository;

import com.portfolio.ecommerce.entity.Customer;
import com.portfolio.ecommerce.entity.CustomerOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderhistorytest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CustomerOrderRepositoryIntegrationTest {

    @Autowired private CustomerRepository customers;
    @Autowired private CustomerOrderRepository orders;

    @Test
    void returnsOnlyRequestedCustomersOrdersNewestFirst() {
        Customer first = customers.save(new Customer("First Customer", "first-integration@example.com"));
        Customer second = customers.save(new Customer("Second Customer", "second-integration@example.com"));

        saveOrder(first, "2026-01-01T10:00:00Z", "10.00");
        saveOrder(second, "2026-01-03T10:00:00Z", "30.00");
        saveOrder(first, "2026-01-02T10:00:00Z", "20.00");

        List<CustomerOrder> result = orders.findByCustomer_IdOrderByCreatedAtDesc(first.id);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("20.00"), result.get(0).total);
        assertEquals(new BigDecimal("10.00"), result.get(1).total);
        assertTrue(result.stream().allMatch(order -> order.customer.id.equals(first.id)));
    }

    @Test
    void returnsEmptyListForCustomerWithNoOrders() {
        Customer customer = customers.save(new Customer("No Orders", "empty-integration@example.com"));
        assertTrue(orders.findByCustomer_IdOrderByCreatedAtDesc(customer.id).isEmpty());
    }

    private void saveOrder(Customer customer, String createdAt, String total) {
        CustomerOrder order = new CustomerOrder();
        order.customer = customer;
        order.createdAt = Instant.parse(createdAt);
        order.total = new BigDecimal(total);
        orders.save(order);
    }
}
