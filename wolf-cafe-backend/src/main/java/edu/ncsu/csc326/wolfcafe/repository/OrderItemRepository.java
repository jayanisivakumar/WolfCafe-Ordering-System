package edu.ncsu.csc326.wolfcafe.repository;

import edu.ncsu.csc326.wolfcafe.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for OrderItems.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
