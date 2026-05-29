package edu.ncsu.csc326.wolfcafe.entity;

/**
 * Enumeration of tip types available for orders.
 */
public enum TipType {
    /** Tip as a percentage of subtotal */
    PERCENTAGE,
    
    /** Tip as a custom dollar amount */
    CUSTOM,
    
    /** No tip */
    NONE
}
