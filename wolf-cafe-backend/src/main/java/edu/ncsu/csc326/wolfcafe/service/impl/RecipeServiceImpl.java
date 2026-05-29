/**
 * Represents the RecipeService of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.IngredientMapper;
import edu.ncsu.csc326.wolfcafe.mapper.RecipeMapper;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Implementation of the RecipeService interface.
 *
 * @author Monica Jin
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    /**
     * Default constructor
     *
     */
    public RecipeServiceImpl () {
    }

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Creates a recipe with the given information. UC2 validations: * too many
     * (>= 3) * duplicate name * invalid price * no ingredients * invalid unit *
     * invalid ingredient name (blank)
     *
     * @param recipeDto
     *            recipe to create
     * @return created recipe
     */
    @Override
    public RecipeDto createRecipe ( final RecipeDto recipeDto ) {
        // Too many
        if ( recipeRepository.count() >= 3 ) {
            throw new IllegalArgumentException( "Cannot add more than 3 recipes to the system" );
        }

        // No name
        if ( recipeDto.getName() == null || recipeDto.getName().trim().isEmpty() ) {
            throw new IllegalArgumentException( "Recipe name cannot be empty" );
        }

        // Duplicate
        if ( recipeRepository.findByName( recipeDto.getName() ).isPresent() ) {
            throw new IllegalArgumentException( "Cannot add a duplicate item" );
        }

        // Invalid price
        if ( recipeDto.getPrice() == null || recipeDto.getPrice() <= 0 ) {
            throw new IllegalArgumentException( "Invalid price, has to be a positive integer" );
        }

        // No ingredients added
        if ( recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty() ) {
            throw new IllegalArgumentException( "Cannot have no ingredients" );
        }

        // Invalid ingredient unit or name
        for ( final var i : recipeDto.getIngredients() ) {
            if ( i.getName() == null || i.getName().trim().isEmpty() ) {
                throw new IllegalArgumentException( "Ingredient name cannot be empty" );
            }
            if ( i.getAmount() == null || i.getAmount() <= 0 ) {
                throw new IllegalArgumentException( "Invalid unit, has to be a positive integer" );
            }
        }

        final Recipe recipe = RecipeMapper.mapToRecipe( recipeDto );
        final Recipe savedRecipe = recipeRepository.save( recipe );
        return RecipeMapper.mapToRecipeDto( savedRecipe );
    }

    /**
     * Returns the recipe with the given id.
     *
     * @param recipeId
     *            recipe's id
     * @return the recipe with the given id
     * @throws ResourceNotFoundException
     *             if the recipe doesn't exist
     */
    @Override
    public RecipeDto getRecipeById ( final Long recipeId ) {
        final Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );
        return RecipeMapper.mapToRecipeDto( recipe );
    }

    /**
     * Returns the recipe with the given name
     *
     * @param recipeName
     *            recipe's name
     * @return the recipe with the given name.
     * @throws ResourceNotFoundException
     *             if the recipe doesn't exist
     */
    @Override
    public RecipeDto getRecipeByName ( final String recipeName ) {
        final Recipe recipe = recipeRepository.findByName( recipeName )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with name " + recipeName ) );
        return RecipeMapper.mapToRecipeDto( recipe );
    }

    /**
     * Returns true if the recipe already exists in the database.
     *
     * @param recipeName
     *            recipe's name to check
     * @return true if already in the database
     */
    @Override
    public boolean isDuplicateName ( final String recipeName ) {
        try {
            getRecipeByName( recipeName );
            return true;
        }
        catch ( final ResourceNotFoundException e ) {
            return false;
        }
    }

    /**
     * Returns a list of all the recipes
     *
     * @return all the recipes
     */
    @Override
    public List<RecipeDto> getAllRecipes () {
        final List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream().map( ( recipe ) -> RecipeMapper.mapToRecipeDto( recipe ) )
                .collect( Collectors.toList() );
    }

    /**
     * Updates the recipe with the given id with the recipe information
     *
     * @param recipeId
     *            id of recipe to update
     * @param recipeDto
     *            values to update
     * @return updated recipe
     * @throws ResourceNotFoundException
     *             if the recipe doesn't exist
     */
    @Override
    public RecipeDto updateRecipe ( final Long recipeId, final RecipeDto recipeDto ) {
        final Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );

        // No name
        if ( recipeDto.getName() == null || recipeDto.getName().trim().isEmpty() ) {
            throw new IllegalArgumentException( "Recipe name cannot be empty" );
        }

        // Duplicate
        final var existing = recipeRepository.findByName( recipeDto.getName() );
        if ( existing.isPresent() && !existing.get().getId().equals( recipeId ) ) {
            throw new IllegalArgumentException( "Cannot add a duplicate recipe" );
        }

        // Invalid price
        if ( recipeDto.getPrice() == null || recipeDto.getPrice() <= 0 ) {
            throw new IllegalArgumentException( "Invalid price, has to be a positive integer" );
        }

        // No ingredients added
        if ( recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty() ) {
            throw new IllegalArgumentException( "Cannot have no ingredients" );
        }

        // Invalid ingredient unit or name
        for ( final var i : recipeDto.getIngredients() ) {
            if ( i.getName() == null || i.getName().trim().isEmpty() ) {
                throw new IllegalArgumentException( "Ingredient name cannot be empty" );
            }
            if ( i.getAmount() == null || i.getAmount() <= 0 ) {
                throw new IllegalArgumentException( "Invalid unit, has to be a positive integer" );
            }
        }

        recipe.setName( recipeDto.getName() );
        recipe.setPrice( recipeDto.getPrice() );
        recipe.getIngredients().clear();
        recipeDto.getIngredients().forEach( dto -> recipe.addIngredient( IngredientMapper.mapToIngredient( dto ) ) );

        final Recipe savedRecipe = recipeRepository.save( recipe );

        return RecipeMapper.mapToRecipeDto( savedRecipe );
    }

    /**
     * Deletes the recipe with the given id
     *
     * @param recipeId
     *            recipe's id
     * @throws ResourceNotFoundException
     *             if the recipe doesn't exist
     */
    @Override
    public void deleteRecipe ( final Long recipeId ) {
        final Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );

        recipeRepository.delete( recipe );
    }
}
