package edu.ncsu.csc326.wolfcafe.service;
 
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
 
/**
 * Interface defining the inventory behaviors.
 */
public interface InventoryService {
 
    /**
     * Creates the inventory.
     * @param inventoryDto inventory to create
     * @return updated inventory after creation
     */
    InventoryDto createInventory(InventoryDto inventoryDto);
 
    /**
     * Returns the single inventory. Creates an empty one if none exists.
     * @return the single inventory
     */
    InventoryDto getInventory();
 
    /**
     * Updates the contents of the inventory by adding the supplied amounts
     * to the current values.
     * @param inventoryDto values to add
     * @return updated inventory
     * @throws IllegalArgumentException if any quantity is non-positive
     */
    InventoryDto updateInventory(InventoryDto inventoryDto);
 
    /**
     * Returns true if the given quantity is a valid (positive) integer.
     * Used by the controller to validate input before delegating to the service.
     * @param qty quantity to validate
     * @return true if qty &gt; 0
     */
    boolean validateQuantity(int qty);
}
