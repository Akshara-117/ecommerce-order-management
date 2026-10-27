package com.portfolio.ecommerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portfolio.ecommerce.entity.Product;
public interface ProductRepository extends JpaRepository<Product,Long> {
}
