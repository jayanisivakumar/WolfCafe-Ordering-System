/**
 * Test class of the MakeRecipeService of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;

/**
 * Tests MakeRecipeService behavior for updating inventory.
 */
@SpringBootTest
class MakeRecipeServiceTest {

    /** Service under test */
    @Autowired
    private MakeRecipeService   makeRecipeService;

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryRepository inventoryRepository;

    /** Inventory used for testing */
    private Inventory           inventory;

    /** Service used for testing */
    @Autowired
    private InventoryService    inventoryService;

    /**
     * Sets up the test case.
     */
    @BeforeEach
    public void setUp () {
        inventoryRepository.deleteAll();

        inventory = new Inventory();
        inventory.getIngredients().put( "TEA", 20 );
        inventory.getIngredients().put( "CIDER", 15 );
        inventory.getIngredients().put( "PUMPKIN_SPICE", 10 );
        inventory.getIngredients().put( "VANILLA", 8 );

        inventory = inventoryRepository.save( inventory );
    }

    /**
     * Test for making a recipe when inventory is sufficient.
     */
    @Test
    @Transactional
    public void testMakeRecipeSuccess () {
        InventoryDto invDto = InventoryMapper.mapToInventoryDto( inventory );

        RecipeDto recipe = new RecipeDto();
        recipe.setName( "AutumnSpecial" );
        recipe.setPrice( 50 );

        recipe.getIngredients().add( new IngredientDto( "TEA", 5 ) );
        recipe.getIngredients().add( new IngredientDto( "PUMPKIN_SPICE", 3 ) );
        recipe.getIngredients().add( new IngredientDto( "VANILLA", 2 ) );

        boolean result = makeRecipeService.makeRecipe( invDto, recipe );
        assertTrue( result );

        Inventory updated = inventoryRepository.findById( inventory.getId() ).orElseThrow();

        assertEquals( 15, updated.getIngredients().get( "TEA" ) );
        assertEquals( 7, updated.getIngredients().get( "PUMPKIN_SPICE" ) );
        assertEquals( 6, updated.getIngredients().get( "VANILLA" ) );
        assertEquals( 15, updated.getIngredients().get( "CIDER" ) ); // unchanged
    }

    /**
     * Test for when single ingredient amount is insufficient.
     */
    @Test
    @Transactional
    public void testMakeRecipeNotEnoughSingle () {
        InventoryDto invDto = InventoryMapper.mapToInventoryDto( inventory );

        RecipeDto recipe = new RecipeDto();
        recipe.setName( "BigTea" );
        recipe.setPrice( 10 );

        recipe.getIngredients().add( new IngredientDto( "TEA", 100 ) ); // too
                                                                        // much

        boolean result = makeRecipeService.makeRecipe( invDto, recipe );
        assertFalse( result );
    }

    /**
     * Test for when multiple ingredients are insufficient.
     */
    @Test
    @Transactional
    public void testMakeRecipeNotEnoughMultiple () {
        InventoryDto invDto = InventoryMapper.mapToInventoryDto( inventory );

        RecipeDto recipe = new RecipeDto();
        recipe.setName( "MegaDrink" );
        recipe.setPrice( 10 );

        recipe.getIngredients().add( new IngredientDto( "TEA", 50 ) );
        recipe.getIngredients().add( new IngredientDto( "CIDER", 50 ) );

        boolean result = makeRecipeService.makeRecipe( invDto, recipe );
        assertFalse( result );
    }

    /**
     * Test for updating inventory when inventory does not exist.
     */
    @Test
    @Transactional
    public void testUpdateInventoryNotFound () {
        inventoryRepository.deleteAll();

        InventoryDto dto = new InventoryDto();
        dto.setId( 1L );

        assertThrows( ResourceNotFoundException.class, () -> inventoryService.updateInventory( dto ) );
    }

}
