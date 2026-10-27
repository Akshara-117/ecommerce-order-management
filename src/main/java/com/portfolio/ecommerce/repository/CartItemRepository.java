package com.portfolio.ecommerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portfolio.ecommerce.entity.CartItem;
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
 java.util.List<CartItem> findByCustomerId(Long customerId);
 void deleteByCustomerId(Long customerId);
 java.util.Optional<CartItem> findByCustomerIdAndProductId(Long customerId,Long productId);
}
