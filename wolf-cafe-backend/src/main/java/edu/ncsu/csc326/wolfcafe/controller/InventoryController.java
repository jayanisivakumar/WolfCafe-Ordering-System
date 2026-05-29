/**
 * Represents the Inventory Controller of the CoffeeMaker system (UC3).
 */
package edu.ncsu.csc326.wolfcafe.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
 
/**
 * Controller for CoffeeMaker's inventory.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
 
    /**
     * Default constructor
     */
    public InventoryController() {
    }
 
    /** Connection to inventory service for manipulating the Inventory model */
    @Autowired
    private InventoryService inventoryService;
 
    /**
     * Returns the current inventory levels for all ingredients.
     * Restricted to STAFF role per UC3.
     * Corresponds to UC3 Steps 13-14: Staff navigates to inventory management page
     * and the system displays current inventory levels.
     *
     * @return current inventory as InventoryDto
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<InventoryDto> getInventory() {
        final InventoryDto inventoryDto = inventoryService.getInventory();
        return ResponseEntity.ok(inventoryDto);
    }
 
    /**
     * Updates the inventory by adding the supplied ingredient amounts to current
     * levels. Restricted to STAFF role per UC3.
     * Corresponds to UC3 Steps 15-17: Staff selects an ingredient, enters a new
     * quantity, and the system validates, updates, and logs [username, STAFF, UPDATE_INVENTORY].
     *
     * Returns 400 Bad Request if any quantity is negative or non-numeric (zero included).
     *
     * @param inventoryDto ingredient amounts to add to current inventory
     * @return updated inventory
     */
    @PreAuthorize("hasRole('STAFF')")
    @PutMapping
    public ResponseEntity<?> updateInventory(@RequestBody final InventoryDto inventoryDto) {
        // Validate all quantities before delegating to the service layer
        for (final Integer qty : inventoryDto.getIngredients().values()) {
            if (qty == null || !inventoryService.validateQuantity(qty)) {
                return new ResponseEntity<>(
                        "Ingredient units must be positive integers.",
                        HttpStatus.BAD_REQUEST);
            }
        }
 
        try {
            final InventoryDto savedInventoryDto = inventoryService.updateInventory(inventoryDto);
            return ResponseEntity.ok(savedInventoryDto);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
 