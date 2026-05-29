package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * This maps between Ingredient and IngredientDto
 *
 * @author Monica Jin
 */

public class IngredientMapper {

    /**
     * Default constructor for IngredientMapper.
     */
    public IngredientMapper () {
    }

    /**
     * Maps from Ingredient to IngredientDto
     *
     * @param ingredient
     *            the ingredient to map
     * @return the IngredientDto
     */
    public static IngredientDto mapToIngredientDto ( final Ingredient ingredient ) {
        final IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setId( ingredient.getId() );
        ingredientDto.setName( ingredient.getName() );
        ingredientDto.setAmount( ingredient.getAmount() );
        return ingredientDto;
    }

    /**
     * Maps from IngredientDto to ingredient
     *
     * @param ingredientDto
     *            the IngredientDto to map
     * @return the Ingredient
     */
    public static Ingredient mapToIngredient ( final IngredientDto ingredientDto ) {
        final Ingredient ingredient = new Ingredient();
        ingredient.setId( ingredientDto.getId() );
        ingredient.setName( ingredientDto.getName() );
        ingredient.setAmount( ingredientDto.getAmount() );
        return ingredient;
    }

}
