/**
 * Represents the Recipe of the CoffeeMaker system.
 * 
 * @author Jayani Sivakumar
 * @author Monica Jin
 */
package edu.ncsu.csc326.wolfcafe.entity;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Recipe for the coffee maker. Recipe is a Data Access Object (DAO) is tied to the database using
 * Hibernate libraries. RecipeRepository provides the methods for database CRUD operations.
 */
@Entity
@Table(name = "recipes")
public class Recipe {

    /** Recipe id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long    id;

    /** Recipe name */
    private String name;

    /** Recipe price */
    private Integer price;
    
    /** List of Ingredients */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ingredient> ingredients = new ArrayList<>();
    
    /**
     * Creates a default recipe for the coffee maker.
     */
    public Recipe () {
        this.name = "";
    }
    
    /**
     * Creates a recipe from all the fields
     * @param id recipe id
     * @param name recipe name
     * @param price recipe price
     */
    public Recipe(Long id, String name, Integer price) {
    	this.id = id;
    	this.name = name;
    	this.price = price;
    	this.ingredients = new ArrayList<>();
    }
    
    /**
     * Creates a recipe from all the fields
     * @param name recipe name
     * @param price recipe price
     */
    public Recipe(String name, Integer price) {
    	this.name = name;
    	this.price = price;
    	this.ingredients = new ArrayList<>();
    }

    /**
     * Get the ID of the Recipe
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID of the Recipe (Used by Hibernate)
     *
     * @param id
     *            the ID
     */
    @SuppressWarnings ( "unused" )
    private void setId ( final Long id ) {
        this.id = id;
    }

    /**
     * Returns name of the recipe.
     *
     * @return Returns the name.
     */
    public String getName () {
        return name;
    }

    /**
     * Sets the recipe name.
     *
     * @param name
     *            The name to set.
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * Returns the price of the recipe.
     *
     * @return Returns the price.
     */
    public Integer getPrice () {
        return price;
    }

    /**
     * Sets the recipe price.
     *
     * @param price
     *            The price to set.
     */
    public void setPrice ( final Integer price ) {
        this.price = price;
    }

    /**
     * Adds ingredient to recipe
     * @param ingredient the ingredient to add
     */
	public void addIngredient(Ingredient ingredient) {
		if (ingredient != null) {
			this.ingredients.add(ingredient);
		}
	}

	/**
	 * gets all of the ingredients in recipe
	 * @return the recipe list
	 */
	public List<Ingredient> getIngredients() {
		return ingredients;
	}
	
}