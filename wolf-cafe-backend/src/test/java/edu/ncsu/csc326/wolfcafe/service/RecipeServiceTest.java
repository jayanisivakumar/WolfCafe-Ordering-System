/**
 * Test class of the RecipeService of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 * @author sohinidas
 */
package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;

/**
 * Tests RecipeService.
 *
 * @author sohinidas
 */
@SpringBootTest
@Transactional
public class RecipeServiceTest {

    /**
     * Default constructor
     *
     */
    public RecipeServiceTest () {

    }

    /** Reference to RecipeService for testing. */
    @Autowired
    private RecipeService    recipeService;

    /** Reference to RecipeRepository for direct database access in tests. */
    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Sets up the test case.
     *
     * @throws java.lang.Exception
     *             if error
     */
    @BeforeEach
    public void setUp () throws Exception {
        recipeRepository.deleteAll();
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#createRecipe(edu.ncsu.csc326.coffee_maker.dto.RecipeDto)}.
     */
    @Test
    @Transactional
    void testCreateRecipe () {
        final RecipeDto recipeDto = new RecipeDto( "Coffee", 50 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        final RecipeDto savedRecipe = recipeService.createRecipe( recipeDto );

        assertAll( "Recipe contents", () -> assertNotNull( savedRecipe.getId() ),
                () -> assertEquals( "Coffee", savedRecipe.getName() ), () -> assertEquals( 50, savedRecipe.getPrice() ),
                () -> assertNotNull( savedRecipe.getIngredients() ),
                () -> assertEquals( 2, savedRecipe.getIngredients().size() ) );

        final RecipeDto retrievedRecipe = recipeService.getRecipeById( savedRecipe.getId() );
        assertAll( "Recipe contents", () -> assertEquals( savedRecipe.getId(), retrievedRecipe.getId() ),
                () -> assertEquals( "Coffee", retrievedRecipe.getName() ),
                () -> assertEquals( 50, retrievedRecipe.getPrice() ),
                () -> assertEquals( 2, retrievedRecipe.getIngredients().size() ) );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#getRecipeById(java.lang.Long)}.
     */
    @Test
    @Transactional
    void testGetRecipeById () {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 75 );
        recipeDto.getIngredients().add( new IngredientDto( "MOCHA", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        final RecipeDto savedRecipe = recipeService.createRecipe( recipeDto );

        final RecipeDto retrieved = recipeService.getRecipeById( savedRecipe.getId() );

        assertNotNull( retrieved );
        assertEquals( "Mocha", retrieved.getName() );
        assertEquals( 75, retrieved.getPrice() );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#getRecipeByName(java.lang.String)}.
     */
    @Test
    @Transactional
    void testGetRecipeByName () {
        final RecipeDto recipeDto = new RecipeDto( "Latte", 60 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        recipeService.createRecipe( recipeDto );

        final RecipeDto retrieved = recipeService.getRecipeByName( "Latte" );

        assertNotNull( retrieved );
        assertEquals( "Latte", retrieved.getName() );
        assertEquals( 60, retrieved.getPrice() );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#isDuplicateName(java.lang.String)}.
     */
    @Test
    @Transactional
    void testIsDuplicateName () {
        final RecipeDto recipeDto = new RecipeDto( "Espresso", 40 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        recipeService.createRecipe( recipeDto );

        assertTrue( recipeService.isDuplicateName( "Espresso" ) );

        assertFalse( recipeService.isDuplicateName( "Cappuccino" ) );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#getAllRecipes()}.
     */
    @Test
    @Transactional
    void testGetAllRecipes () {
        assertEquals( 0, recipeService.getAllRecipes().size() );

        recipeService.createRecipe( new RecipeDto( "Coffee", 50, List.of( new IngredientDto( "COFFEE", 5 ) ) ) );
        recipeService.createRecipe( new RecipeDto( "Mocha", 75,
                List.of( new IngredientDto( "COFFEE", 5 ), new IngredientDto( "CHOCOLATE", 2 ) ) ) );
        recipeService.createRecipe( new RecipeDto( "Latte", 600,
                List.of( new IngredientDto( "COFFEE", 5 ), new IngredientDto( "MILK", 4 ) ) ) );

        assertEquals( 3, recipeService.getAllRecipes().size() );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#updateRecipe(java.lang.Long, edu.ncsu.csc326.coffee_maker.dto.RecipeDto)}.
     */
    @Test
    @Transactional
    void testUpdateRecipe () {
        final RecipeDto recipeDto = new RecipeDto( "Coffee", 50 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        final RecipeDto savedRecipe = recipeService.createRecipe( recipeDto );

        final RecipeDto updatedDto = new RecipeDto( "Coffee Deluxe", 55 );
        updatedDto.getIngredients().add( new IngredientDto( "COFFEE", 7 ) );
        updatedDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        final RecipeDto updatedRecipe = recipeService.updateRecipe( savedRecipe.getId(), updatedDto );

        assertAll( "Updated recipe contents", () -> assertEquals( savedRecipe.getId(), updatedRecipe.getId() ),
                () -> assertEquals( "Coffee Deluxe", updatedRecipe.getName() ),
                () -> assertEquals( 55, updatedRecipe.getPrice() ) );
    }

    /**
     * Test method for
     * {@link edu.ncsu.csc326.coffee_maker.services.impl.RecipeServiceImpl#deleteRecipe(java.lang.Long)}.
     */
    @Test
    @Transactional
    void testDeleteRecipe () {
        final RecipeDto recipeDto = new RecipeDto( "Coffee", 50 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );
        final RecipeDto savedRecipe = recipeService.createRecipe( recipeDto );
        assertNotNull( recipeService.getRecipeById( savedRecipe.getId() ) );

        recipeService.deleteRecipe( savedRecipe.getId() );

        assertThrows( edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException.class,
                () -> recipeService.getRecipeById( savedRecipe.getId() ) );
    }

    /**
     * Test method for updateRecipe when recipe doesn't exist
     */
    @Test
    @Transactional
    void testUpdateRecipeNotFound () {
        final RecipeDto updatedDto = new RecipeDto( "Coffee", 55 );

        assertThrows( edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException.class,
                () -> recipeService.updateRecipe( 999L, updatedDto ) );
    }

    /**
     * Test method for deleteRecipe when recipe doesn't exist
     */
    @Test
    @Transactional
    void testDeleteRecipeNotFound () {
        assertThrows( edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException.class,
                () -> recipeService.deleteRecipe( 999L ) );
    }
}
