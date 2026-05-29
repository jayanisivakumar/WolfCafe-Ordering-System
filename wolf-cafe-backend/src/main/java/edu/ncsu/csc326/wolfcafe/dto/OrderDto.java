package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object for an order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    /** Order id */
    private Long id;

    /** Customer name */
    private String customerName;

    /** List of items in the order */
    private List<OrderItemDto> items;
    
    /** Exposes fullfilled time for frontend*/
    private LocalDateTime fulfilledAt;

    /** Timestamp when the customer picked up the order */
    private LocalDateTime pickedUpAt;

    /** Timestamp when the order was cancelled */
    private LocalDateTime cancelledAt;

    /** Subtotal before tax and tip */
    private double subtotal;

    /** Sales tax amount */
    private double tax;

    /** Tip amount */
    private double tip;

    /** Total including subtotal, tax, and tip */
    private double total;

    /** Order status */
    private String status;

    /** Timestamp when the order was created */
    private LocalDateTime createdAt;

    /** Message for the API response */
    private String message;
}