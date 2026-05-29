/**
 * Represents the RecipeDto of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to transfer Recipe data between the client and server. This class will
 * serve as the response in the REST API.
 */
public class RecipeDto {

    /** Recipe Id */
    private Long                id;

    /** Recipe name */
    private String              name;

    /** Recipe price */
    private Integer             price;

    /** List of Ingredients */
    private List<IngredientDto> ingredients = new ArrayList<>();

    /**
     * Default constructor for Recipe.
     */
    public RecipeDto () {

    }

    /**
     * Creates a recipe from all the fields
     *
     * @param id
     *            recipe id
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     * @param ingredients
     *            recipe ingredients
     */
    public RecipeDto ( final Long id, final String name, final Integer price, final List<IngredientDto> ingredients ) {
        this.id = id;
        this.name = name;
        this.price = price;
        if ( ingredients != null ) {
            this.ingredients = ingredients;
        }
    }

    /**
     * Creates a recipe from all the fields except id
     *
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     * @param ingredients
     *            recipe ingredients
     */
    public RecipeDto ( final String name, final Integer price, final List<IngredientDto> ingredients ) {
        this.name = name;
        this.price = price;
        if ( ingredients != null ) {
            this.ingredients = ingredients;
        }
    }

    /**
     * Creates a recipe from all the fields
     *
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     */
    public RecipeDto ( final String name, final Integer price ) {
        this.name = name;
        this.price = price;
    }

    /**
     * Gets the recipe id.
     *
     * @return the id
     */
    public Long getId () {
        return id;
    }

    /**
     * Recipe id to set.
     *
     * @param id
     *            the id to set
     */
    public void setId ( final Long id ) {
        this.id = id;
    }

    /**
     * Gets recipe's name
     *
     * @return the name
     */
    public String getName () {
        return name;
    }

    /**
     * Recipe name to set.
     *
     * @param name
     *            the name to set
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * Gets the recipe's price
     *
     * @return the price
     */
    public Integer getPrice () {
        return price;
    }

    /**
     * Prices value to set.
     *
     * @param price
     *            the price to set
     */
    public void setPrice ( final Integer price ) {
        this.price = price;
    }

    /**
     * gets all of the ingredients in recipe
     *
     * @return the recipe list
     */
    public List<IngredientDto> getIngredients () {
        return ingredients;
    }
}
