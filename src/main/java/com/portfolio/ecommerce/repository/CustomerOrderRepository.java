package com.portfolio.ecommerce.repository;

import com.portfolio.ecommerce.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    // Filter by customer in SQL instead of loading all orders into application memory.
    List<CustomerOrder> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
}
