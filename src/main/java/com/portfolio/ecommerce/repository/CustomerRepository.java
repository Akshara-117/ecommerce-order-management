package com.portfolio.ecommerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portfolio.ecommerce.entity.Customer;
public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
