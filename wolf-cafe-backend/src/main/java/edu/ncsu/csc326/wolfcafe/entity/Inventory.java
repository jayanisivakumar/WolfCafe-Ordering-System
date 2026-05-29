/**
 * Represents the Inventory of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.entity;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

/**
 * Inventory for the coffee maker. Inventory is a Data Access Object (DAO) is
 * tied to the database using Hibernate libraries. InventoryRepository provides
 * the methods for database CRUD operations.
 *
 * This implementation supports arbitrary ingredient types using a Map with
 * String, Integer.
 */
@Entity
public class Inventory {

    /** id for inventory entry */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long                 id;

    /**
     * This implementation supports arbitrary ingredient types using a
     * Map&lt;String, Integer&gt;.
     */
    @ElementCollection
    @CollectionTable ( name = "inventory_ingredients", joinColumns = @JoinColumn ( name = "inventory_id" ) )
    private Map<String, Integer> ingredients = new HashMap<>();

    /**
     * Empty constructor for Hibernate
     */
    public Inventory () {
        // Intentionally empty so that Hibernate can instantiate
        // Inventory object.
    }

    /**
     * Constructor with id and ingredients
     *
     * @param id
     *            inventory's id
     * @param ingredients
     *            map of ingredient name to quantity
     */
    public Inventory ( final Long id, final Map<String, Integer> ingredients ) {
        this.id = id;
        this.ingredients = ingredients != null ? ingredients : new HashMap<>();
    }

    /**
     * Returns the ID of the entry in the DB
     *
     * @return long
     */
    public Long getId () {
        return id;
    }

    /**
     * Set the ID of the Inventory (Used by Hibernate)
     *
     * @param id
     *            the ID
     */
    public void setId ( final Long id ) {
        this.id = id;
    }

    /**
     * Returns all ingredients currently stored in inventory.
     *
     * @return map of ingredient name to quantity
     */
    public Map<String, Integer> getIngredients () {
        return ingredients;
    }

    /**
     * Sets the entire ingredient map.
     *
     * @param ingredients
     *            map of ingredient name to quantity
     */
    public void setIngredients ( final Map<String, Integer> ingredients ) {
        this.ingredients = ingredients;
    }

}
