package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data transfer object for placing a new order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlaceDto {

    /** List of items to order, each with itemId and quantity */
    private List<OrderItemRequestDto> items;

    /** Tip type: PERCENTAGE, CUSTOM, or NONE */
    private String tipType;

    /** Tip value: percentage (15, 20, 25) or custom dollar amount */
    private double tipValue;
}
