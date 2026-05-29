package edu.ncsu.csc326.wolfcafe.repository;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for Orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders for a specific customer.
     * @param customerId the customer's user id
     * @return list of orders for that customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Finds all orders with a specific status.
     * @param status the order status to search for
     * @return list of orders with that status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds all orders for a customer with a specific status.
     * @param customerId the customer's user id
     * @param status the order status to search for
     * @return list of orders for that customer with that status
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
}
