package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object for an item within an order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    /** Item id */
    private Long itemId;

    /** Item name */
    private String name;

    /** Quantity of this item */
    private int quantity;

    /** Price per unit */
    private double price;
}
