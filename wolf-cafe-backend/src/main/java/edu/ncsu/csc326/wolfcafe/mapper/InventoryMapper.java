/**
 * Represents the Inventory Mapper of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.mapper;

import java.util.HashMap;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;

/**
 * Converts between InventoryDto and Inventory entity.
 */
public class InventoryMapper {

    /**
     * Default constructor
     *
     */
    public InventoryMapper () {
    }

    /**
     * Converts an Inventory entity to InventoryDto
     *
     * @param inventory
     *            Inventory to convert
     * @return InventoryDto object
     */
    public static InventoryDto mapToInventoryDto ( final Inventory inventory ) {
        if ( inventory == null ) {
            return null;
        }

        final InventoryDto dto = new InventoryDto();
        dto.setId( inventory.getId() );

        // ingredient map
        dto.setIngredients( new HashMap<>( inventory.getIngredients() ) );

        return dto;
    }

    /**
     * Converts an InventoryDto to an Inventory entity
     *
     * @param inventoryDto
     *            InventoryDto to convert
     * @return Inventory entity
     */
    public static Inventory mapToInventory ( final InventoryDto inventoryDto ) {
        if ( inventoryDto == null ) {
            return null;
        }

        final Inventory inventory = new Inventory();
        inventory.setId( inventoryDto.getId() );

        // ingredient map
        inventory.setIngredients( new HashMap<>( inventoryDto.getIngredients() ) );

        return inventory;

    }
}
