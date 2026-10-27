package com.portfolio.ecommerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portfolio.ecommerce.entity.CustomerOrder;
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder,Long> {
}
