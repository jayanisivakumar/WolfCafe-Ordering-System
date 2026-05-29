/**
 * Represents the RecipeMapper of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.mapper;

import java.util.stream.Collectors;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;

/**
 * Converts between RecipeDto and Recipe entity
 */
public class RecipeMapper {

    /**
     * Default constructor
     *
     */
    public RecipeMapper () {
    }

    /**
     * Converts a Recipe entity to RecipeDto
     *
     * @param recipe
     *            Recipe to convert
     * @return RecipeDto object
     */
    public static RecipeDto mapToRecipeDto ( final Recipe recipe ) {
        return new RecipeDto( recipe.getId(), recipe.getName(), recipe.getPrice(), recipe.getIngredients().stream()
                .map( IngredientMapper::mapToIngredientDto ).collect( Collectors.toList() ) );

    }

    /**
     * Converts a RecipeDto object to a Recipe entity.
     *
     * @param recipeDto
     *            RecipeDto to convert
     * @return Recipe entity
     */
    public static Recipe mapToRecipe ( final RecipeDto recipeDto ) {
        final Recipe recipe = new Recipe( recipeDto.getId(), recipeDto.getName(), recipeDto.getPrice() );

        if ( recipeDto.getIngredients() != null ) {
            recipeDto.getIngredients()
                    .forEach( dto -> recipe.addIngredient( IngredientMapper.mapToIngredient( dto ) ) );
        }

        return recipe;
    }

}
