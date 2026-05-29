package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderPlaceDto;

import java.util.List;

/**
 * Service interface for order operations (UC5 and UC6).
 */
public interface OrderService {

    /**
     * Places a new order for the authenticated customer.
     * Validates items, inventory, and tip. Deducts inventory and logs the action.
     * 
     * @param orderPlaceDto the order details including items and tip
     * @return the created order
     * @throws IllegalArgumentException if order is empty, tip is invalid, or inventory insufficient
     * @throws ResourceNotFoundException if an item is not found
     */
    OrderDto placeOrder(OrderPlaceDto orderPlaceDto);

    /**
     * Gets all pending orders for staff fulfillment (UC4).
     * 
     * @return list of all pending orders
     */
    List<OrderDto> getPendingOrders();

    /**
     * Gets an order by ID. Accessible by staff, admin, or the customer who placed it.
     * 
     * @param id the order id
     * @return the order details
     * @throws ResourceNotFoundException if order not found
     */
    OrderDto getOrder(Long id);

    /**
     * Gets all orders for the authenticated customer (UC6).
     * 
     * @return list of the customer's orders
     */
    List<OrderDto> getMyOrders();

    /**
     * Marks an order as fulfilled (ready for pickup). Only callable by staff.
     * 
     * @param orderId the order id
     * @return the updated order
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalArgumentException if order is not pending
     */
    OrderDto fulfillOrder(Long orderId);

    /**
     * Marks an order as picked up by the customer. Only callable by the customer who placed it.
     * 
     * @param orderId the order id
     * @return the updated order
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalArgumentException if order is not ready for pickup
     */
    OrderDto pickupOrder(Long orderId);
    
    /**
     * Cancels a pending or ready-for-pickup order. Only callable by staff.
     *
     * @param orderId the order id
     * @return the updated order
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalArgumentException if order is already picked up or cancelled
     */
    OrderDto cancelOrder(Long orderId);
    
    /**
     * Gets all orders across all statuses (for staff/admin order history).
     * @return list of all orders
     */
    List<OrderDto> getAllOrders();
}
