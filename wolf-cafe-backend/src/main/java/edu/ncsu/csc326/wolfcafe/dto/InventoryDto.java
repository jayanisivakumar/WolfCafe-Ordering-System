/**
 * Represents the InventoryDto of the CoffeeMaker system.
 * 
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to transfer Inventory data between the client and server.  
 * This class will serve as the response in the REST API.
 * It stores arbitrary ingredient names mapped to their quantities.
 */
public class InventoryDto {
	
	/** id for inventory entry */
    private Long    id;
    
    /** Map of ingredient name to quantity */
    private Map<String, Integer> ingredients = new HashMap<>();
    
    /** 
     * Default InventoryDto constructor.
     */
    public InventoryDto() {
    	// Default InventoryDto constructor.
    }
    
    /**
     * Constructs an InventoryDto with all fields.
     * @param id inventory id
     * @param ingredients map of ingredient name to quantity
     */
	public InventoryDto(Long id, Map<String, Integer> ingredients) {
		this.id = id;
		this.ingredients = ingredients;
	}

	/**
	 * Gets the inventory id.
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Inventory id to set.
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
     * Returns all ingredients in inventory.
     *
     * @return map of ingredient name to quantity
     */
    public Map<String, Integer> getIngredients() {
        return ingredients;
    }

    /**
     * Sets the ingredient map.
     *
     * @param ingredients map of ingredient name to quantity
     */
    public void setIngredients(Map<String, Integer> ingredients) {
        this.ingredients = ingredients;
    }
    
}
