package edu.ncsu.csc326.wolfcafe.entity;

/**
 * Enumeration of possible order statuses in the WolfCafe system.
 */
public enum OrderStatus {
    /** Order has been placed and is pending fulfillment */
    PENDING,
    
    /** Order has been fulfilled and is ready for pickup */
    READY_FOR_PICKUP,
    
    /** Customer has picked up the order */
    PICKED_UP,
    
    /** Order has been cancelled */
    CANCELLED
}
