/**
 * Test class of the MakeRecipeController of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Controller tests for making a recipe through /api/makerecipe.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MakeRecipeControllerTest {

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc             mvc;

    /** Repos/services used to set up DB state */
    @Autowired
    private RecipeRepository    recipeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private RecipeService       recipeService;

    /** Keep track of inventory row in setup */
    private Long                inventoryId;

    /**
     * Sets up the test case.
     *
     * @throws java.lang.Exception
     *             if error
     */
    @BeforeEach
    public void setUp () throws Exception {
        recipeRepository.deleteAll();
        inventoryRepository.deleteAll();

        // Create inventory
        Inventory inv = new Inventory();
        inv.getIngredients().put( "TEA", 10 );
        inv.getIngredients().put( "CIDER", 10 );
        inv.getIngredients().put( "PUMPKIN_SPICE", 10 );
        inv.getIngredients().put( "VANILLA", 10 );

        inv = inventoryRepository.save( inv );
        inventoryId = inv.getId();

        // Create recipe
        RecipeDto recipe = new RecipeDto();
        recipe.setName( "AutumnDrink" );
        recipe.setPrice( 60 );

        recipe.getIngredients().add( new IngredientDto( "TEA", 2 ) );
        recipe.getIngredients().add( new IngredientDto( "PUMPKIN_SPICE", 3 ) );
        recipe.getIngredients().add( new IngredientDto( "VANILLA", 1 ) );

        recipeService.createRecipe( recipe );
    }

    /**
     * Test MakeRecipe for when invenory is sufficient.
     */
    @Test
    @Transactional
    public void testMakeRecipeSuccess () throws Exception {
        mvc.perform( post( "/api/makerecipe/AutumnDrink" ).contentType( MediaType.APPLICATION_JSON ).content( "100" ) )
                .andDo( print() ).andExpect( status().isOk() ).andExpect( content().string( "40" ) );
    }

    /**
     * Test for MakeRecipe when there is not enough money.
     */
    @Test
    @Transactional
    public void testMakeRecipeNotEnoughMoney () throws Exception {
        mvc.perform( post( "/api/makerecipe/AutumnDrink" ).contentType( MediaType.APPLICATION_JSON ).content( "10" ) )
                .andDo( print() ).andExpect( status().isConflict() ).andExpect( content().string( "10" ) );
    }

    /**
     * Test for MakeRecipe when there is money but low on inventory.
     */
    @Test
    @Transactional
    public void testMakeRecipeNoInventory () throws Exception {
        Inventory inv = inventoryRepository.findById( inventoryId ).orElseThrow();

        // set ingredients too low
        inv.getIngredients().put( "TEA", 0 );
        inv.getIngredients().put( "PUMPKIN_SPICE", 0 );
        inv.getIngredients().put( "VANILLA", 0 );

        inventoryRepository.save( inv );

        mvc.perform( post( "/api/makerecipe/AutumnDrink" ).contentType( MediaType.APPLICATION_JSON ).content( "100" ) )
                .andDo( print() ).andExpect( status().isBadRequest() ).andExpect( content().string( "100" ) );
    }

    /**
     * Test for MakeRecipe using invalid payment (negative).
     */
    @Test
    @Transactional
    public void testMakeRecipeInvalidPayment () throws Exception {

        mvc.perform( post( "/api/makerecipe/AutumnDrink" ).contentType( MediaType.APPLICATION_JSON ).content( "-5" ) )
                .andDo( print() ).andExpect( status().isBadRequest() );
    }
}
