package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;

/**
 * REST controller for managing ingredients in the CoffeeMaker system.
 *
 * @author Sohini Das
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/ingredients" )
public class IngredientController {

    /**
     * Default constructor for IngredientController.
     */
    public IngredientController () {
    }

    /** Service layer for ingredient operations */
    @Autowired
    private IngredientService ingredientService;

    /**
     * Creates a new ingredient in the system.
     *
     * @param ingredientDto
     *            the ingredient to create
     * @return the created ingredient
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<IngredientDto> createIngredient ( @RequestBody final IngredientDto ingredientDto ) {
        final IngredientDto savedIngredientDto = ingredientService.createIngredient( ingredientDto );
        return ResponseEntity.ok( savedIngredientDto );
    }

    /**
     * Returns all ingredients in the system.
     *
     * @return list of all ingredients
     */
    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public List<IngredientDto> getIngredients () {
        return ingredientService.getAllIngredients();
    }

    /**
     * Returns the ingredient with the given id.
     *
     * @param id
     *            the id of the ingredient to retrieve
     * @return the ingredient with the given id
     */
    @GetMapping ( "{id}" )
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<IngredientDto> getIngredient ( @PathVariable ( "id" ) final Long id ) {
        return ResponseEntity.ok( ingredientService.getIngredientById( id ) );
    }

    /**
     * Deletes the ingredient with the given id.
     *
     * @param id
     *            the id of the ingredient to delete
     * @return success message
     */
    @DeleteMapping ( "{id}" )
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> deleteIngredient ( @PathVariable ( "id" ) final Long id ) {
        ingredientService.deleteIngredient( id );
        return ResponseEntity.ok( "Ingredient deleted successfully." );
    }

    /**
     * Deletes all ingredients in the system.
     *
     * @return success message
     */
    @DeleteMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> deleteAllIngredients () {
        ingredientService.deleteAllIngredients();
        return ResponseEntity.ok( "All ingredients deleted successfully." );
    }

    /**
     * Handles IllegalArgumentException thrown by the service layer and returns
     * a 400 Bad Request response.
     *
     * @param ex
     *            the exception to handle
     * @return 400 Bad Request with the exception message
     */
    @ExceptionHandler ( IllegalArgumentException.class )
    public ResponseEntity<String> handleIllegalArgument ( final IllegalArgumentException ex ) {
        return ResponseEntity.badRequest().body( ex.getMessage() );
    }
}
