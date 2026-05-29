package edu.ncsu.csc326.wolfcafe.controller;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderPlaceDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for API endpoints for Orders (UC5, UC4, UC6).
 */
@RestController
@RequestMapping("api/orders")
@AllArgsConstructor
@CrossOrigin("*")
public class OrderController {

    private OrderService orderService;

    /**
     * Places a new order for the authenticated customer (UC5).
     * Validates order has at least one item, tip is valid, and inventory is sufficient.
     * Deducts inventory for ordered items.
     *
     * Returns 201 Created on success with order details.
     * Returns 400 if order is empty, tip is invalid, or inventory insufficient.
     * Returns 403 if caller is not a customer.
     *
     * @param orderPlaceDto order details including items and tip
     * @return created order with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderPlaceDto orderPlaceDto) {
        try {
            OrderDto savedOrder = orderService.placeOrder(orderPlaceDto);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets all pending orders for staff fulfillment (UC4).
     * Returns a list of orders with status PENDING.
     *
     * @return list of pending orders
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/pending")
    public ResponseEntity<List<OrderDto>> getPendingOrders() {
        List<OrderDto> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets a single order by ID (UC4, UC6).
     * Accessible by staff, admin, or the customer who placed the order.
     *
     * @param id order id
     * @return order with the given id
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @GetMapping("{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") Long id) {
        try {
            OrderDto order = orderService.getOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets all orders for the authenticated customer (UC6).
     * Returns all orders placed by the logged-in customer.
     *
     * @return list of customer's orders
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        List<OrderDto> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Fulfills a pending order and marks it as ready for pickup (UC4).
     * Staff marks an order as fulfilled so it appears in the customer's pickup queue.
     *
     * Returns 200 with updated order on success.
     * Returns 400 if order is not pending.
     * Returns 403 if caller is not staff.
     *
     * @param id order id
     * @return updated order
     */
    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("{id}/fulfill")
    public ResponseEntity<?> fulfillOrder(@PathVariable("id") Long id) {
        try {
            OrderDto updatedOrder = orderService.fulfillOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (ResourceNotFoundException e) {
        	// Give an informational message upon error
        	Map<String, String> error = new HashMap<>();
        	error.put("error", e.getMessage());
        	return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Customer acknowledges pickup of a fulfilled order (UC6).
     * Marks the order as PICKED_UP in the system.
     *
     * Returns 200 with updated order on success.
     * Returns 400 if order is not ready for pickup.
     * Returns 403 if caller is not the customer who placed the order.
     *
     * @param id order id
     * @return updated order
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("{id}/pickup")
    public ResponseEntity<?> pickupOrder(@PathVariable("id") Long id) {
        try {
            OrderDto updatedOrder = orderService.pickupOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            // Distinguish ownership/guest errors (403) from state errors (400)
            String msg = e.getMessage();
            if (msg.contains("not authorized") || msg.contains("Guest orders")) {
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Cancels a pending or ready-for-pickup order (UC6 alt flow).
     * Staff cancels an order so the customer is notified of cancellation.
     *
     * Returns 200 with updated order on success.
     * Returns 400 if order is already picked up or cancelled.
     * Returns 404 if order not found.
     * Returns 403 if caller is not staff.
     *
     * @param id order id
     * @return updated order with CANCELLED status
     */
    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("id") Long id) {
        try {
            OrderDto updatedOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Gets all orders across all statuses for staff/admin order history.
     *
     * @return list of all orders
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
