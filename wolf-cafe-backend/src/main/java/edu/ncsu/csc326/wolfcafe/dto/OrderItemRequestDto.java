package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object for an item request when placing an order.
 * Contains the item ID and desired quantity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {

    /** ID of the menu item to order */
    private Long itemId;

    /** Quantity of this item to order */
    private int quantity;
}
