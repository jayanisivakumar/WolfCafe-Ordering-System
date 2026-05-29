package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Controller for Recipes.
 *
 * @author Monica Jin
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/recipes" )
public class RecipeController {

    /**
     * Default constructor
     *
     */
    public RecipeController () {
    }

    /** Connection to RecipeService */
    @Autowired
    private RecipeService recipeService;

    /**
     * REST API method to provide GET access to all recipes in the system
     *
     * @return JSON representation of all recipes
     */
    @GetMapping
    public List<RecipeDto> getRecipes () {
        return recipeService.getAllRecipes();
    }

    /**
     * REST API method to provide GET access to a specific recipe, as indicated
     * by the path variable provided (the name of the recipe desired)
     *
     * @param name
     *            recipe name
     * @return response to the request
     */
    @GetMapping ( "name/{name}" )
    public ResponseEntity<RecipeDto> getRecipeByName ( @PathVariable ( "name" ) final String name ) {
        final RecipeDto recipeDto = recipeService.getRecipeByName( name );
        return ResponseEntity.ok( recipeDto );
    }

    /**
     * REST API method to provide GET access to a specific recipe, as indicated
     * by the path variable provided (the id of the recipe desired)
     *
     * @param id
     *            recipe id
     * @return response to the request
     */
    @GetMapping ( "{id}" )
    public ResponseEntity<RecipeDto> getRecipeById ( @PathVariable ( "id" ) final Long id ) {
        final RecipeDto recipeDto = recipeService.getRecipeById( id );
        return ResponseEntity.ok( recipeDto );
    }

    /**
     * Validates and provides error if encountered bad request
     *
     * @param recipeDto
     *            the recipe dto to check
     * @return error message
     */
    private String validateRecipeForCreate ( final RecipeDto recipeDto ) {
        // cannot be null
        if ( recipeDto == null ) {
            return "Recipe cannot be null";
        }

        // name cannot be empty
        if ( recipeDto.getName() == null || recipeDto.getName().trim().isEmpty() ) {
            return "Recipe name cannot be empty";
        }

        // Invalid Price, has to be positive int
        if ( recipeDto.getPrice() == null || recipeDto.getPrice() <= 0 ) {
            return "Invalid price";
        }

        // recipe cannot have no ingredients
        if ( recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty() ) {
            return "No ingredients";
        }

        // units have to be positive ints
        for ( final var i : recipeDto.getIngredients() ) {
            if ( i.getName() == null || i.getName().trim().isEmpty() ) {
                return "Invalid ingredient name";
            }
            if ( i.getAmount() == null || i.getAmount() <= 0 ) {
                return "Invalid unit";
            }
        }

        return null; // valid
    }

    /**
     * REST API method to provide POST access to the Recipe model.
     *
     * @param recipeDto
     *            The valid Recipe to be saved.
     * @return ResponseEntity indicating success if the Recipe could be saved to
     *         the inventory, or an error if it could not be
     */
    @PostMapping
    public ResponseEntity< ? > createRecipe ( @RequestBody final RecipeDto recipeDto ) {
        // duplicate
        if ( recipeService.isDuplicateName( recipeDto.getName() ) ) {
            return new ResponseEntity<>( recipeDto, HttpStatus.CONFLICT );
        }
        // too many
        if ( recipeService.getAllRecipes().size() >= 3 ) {
            return new ResponseEntity<>( recipeDto, HttpStatus.INSUFFICIENT_STORAGE );
        }

        final String error = validateRecipeForCreate( recipeDto );
        if ( error != null ) {
            return new ResponseEntity<>( error, HttpStatus.BAD_REQUEST );
        }

        final RecipeDto savedRecipeDto = recipeService.createRecipe( recipeDto );
        return ResponseEntity.ok( savedRecipeDto );
    }

    /**
     * REST API method to allow deleting a Recipe from the CoffeeMaker's
     * Inventory, by making a DELETE request to the API endpoint and indicating
     * the recipe to delete (as a path variable)
     *
     * @param recipeId
     *            The name of the Recipe to delete
     * @return Success if the recipe could be deleted; an error if the recipe
     *         does not exist
     */
    @DeleteMapping ( "{id}" )
    public ResponseEntity<String> deleteRecipe ( @PathVariable ( "id" ) final Long recipeId ) {
        recipeService.deleteRecipe( recipeId );
        return ResponseEntity.ok( "Recipe deleted successfully." );
    }

    /**
     * This method updates accordingly
     *
     * @param id
     *            given id of recipe
     * @param recipeDto
     *            given recipeDto
     * @return Success if updated correctly
     */
    @PutMapping ( "{id}" )
    public ResponseEntity< ? > updateRecipe ( @PathVariable final Long id, @RequestBody final RecipeDto recipeDto ) {
        // Duplicate
        final RecipeDto existing = recipeService.getRecipeById( id );
        if ( !existing.getName().equals( recipeDto.getName() )
                && recipeService.isDuplicateName( recipeDto.getName() ) ) {
            return new ResponseEntity<>( recipeDto, HttpStatus.CONFLICT );
        }

        final String error = validateRecipeForCreate( recipeDto );
        if ( error != null ) {
            return new ResponseEntity<>( error, HttpStatus.BAD_REQUEST );
        }

        try {
            final RecipeDto updated = recipeService.updateRecipe( id, recipeDto );
            return ResponseEntity.ok( updated );
        }
        catch ( final ResourceNotFoundException e ) {
            // Cannot Edit
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
    }
}
