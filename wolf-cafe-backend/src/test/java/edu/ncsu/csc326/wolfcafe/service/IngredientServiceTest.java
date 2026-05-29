package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.mapper.IngredientMapper;
import jakarta.transaction.Transactional;

/**
 * Tests for IngredientService.
 *
 * @author Sohini Das
 */
@SpringBootTest
public class IngredientServiceTest {

    /** Service layer for ingredient operations */
    @Autowired
    private IngredientService ingredientService;

    /** Service layer for item operations — used to create items that reference ingredients */
    @Autowired
    private ItemService itemService;

    /** Service layer for inventory operations — used to verify inventory cleanup on delete */
    @Autowired
    private InventoryService inventoryService;

    /**
     * Default constructor for IngredientServiceTest.
     */
    public IngredientServiceTest () {
    }

    /**
     * Clears all ingredients before each test.
     *
     * @throws Exception
     *             if setup fails
     */
    @BeforeEach
    public void setUp () throws Exception {
        ingredientService.deleteAllIngredients();
    }

    /**
     * Tests successfully creating ingredients.
     */
    @Test
    @Transactional
    public void testCreateIngredient () {
        final IngredientDto ingredient1 = new IngredientDto( "COFFEE", 5 );
        final IngredientDto createdIngredient1 = ingredientService.createIngredient( ingredient1 );
        assertAll( "Ingredient contents", () -> assertEquals( "COFFEE", createdIngredient1.getName() ),
                () -> assertEquals( 5, createdIngredient1.getAmount() ) );

        final IngredientDto ingredient2 = new IngredientDto( "PUMPKIN SPICE", 10 );
        final IngredientDto createdIngredient2 = ingredientService.createIngredient( ingredient2 );
        assertAll( "Ingredient contents", () -> assertEquals( "PUMPKIN SPICE", createdIngredient2.getName() ),
                () -> assertEquals( 10, createdIngredient2.getAmount() ) );
    }

    /**
     * Tests successfully retrieving an ingredient by id.
     */
    @Test
    @Transactional
    public void testGetIngredientById () {
        final IngredientDto ingredient1 = new IngredientDto( "COFFEE", 5 );
        final IngredientDto createdIngredient1 = ingredientService.createIngredient( ingredient1 );
        final IngredientDto fetchedIngredient1 = ingredientService.getIngredientById( createdIngredient1.getId() );
        assertAll( "Ingredient contents", () -> assertEquals( "COFFEE", fetchedIngredient1.getName() ),
                () -> assertEquals( 5, fetchedIngredient1.getAmount() ) );

        final IngredientDto ingredient2 = new IngredientDto( "PUMPKIN SPICE", 10 );
        final IngredientDto createdIngredient2 = ingredientService.createIngredient( ingredient2 );
        final IngredientDto fetchedIngredient2 = ingredientService.getIngredientById( createdIngredient2.getId() );
        assertAll( "Ingredient contents", () -> assertEquals( "PUMPKIN SPICE", fetchedIngredient2.getName() ),
                () -> assertEquals( 10, fetchedIngredient2.getAmount() ) );
    }

    /**
     * Tests that creating a duplicate ingredient throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_Duplicate () {
        final IngredientDto ingredient1 = new IngredientDto( "COFFEE", 5 );
        ingredientService.createIngredient( ingredient1 );

        final IngredientDto duplicate = new IngredientDto( "COFFEE", 3 );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( duplicate );
        } );
    }

    /**
     * Tests that creating an ingredient with amount zero throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_InvalidAmount_Zero () {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 0 );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( ingredient );
        } );
    }

    /**
     * Tests that creating an ingredient with a negative amount throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_InvalidAmount_Negative () {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", -5 );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( ingredient );
        } );
    }

    /**
     * Tests the Ingredient entity constructor.
     */
    @Test
    @Transactional
    public void testIngredientConstructor () {
        final Ingredient ingredient = new Ingredient( 1L, "COFFEE", 5 );
        assertAll( "Ingredient contents", () -> assertEquals( 1L, ingredient.getId() ),
                () -> assertEquals( "COFFEE", ingredient.getName() ), () -> assertEquals( 5, ingredient.getAmount() ) );
    }

    /**
     * Tests the IngredientDto constructor.
     */
    @Test
    @Transactional
    public void testIngredientDtoConstructor () {
        final IngredientDto dto = new IngredientDto( 1L, "COFFEE", 5 );
        assertAll( "IngredientDto contents", () -> assertEquals( 1L, dto.getId() ),
                () -> assertEquals( "COFFEE", dto.getName() ), () -> assertEquals( 5, dto.getAmount() ) );
    }

    /**
     * Tests successfully deleting an ingredient that is not used by any item.
     * After deletion the ingredient should no longer be retrievable.
     */
    @Test
    @Transactional
    public void testDeleteIngredient () {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        final IngredientDto created = ingredientService.createIngredient( ingredient );
        // No items reference this ingredient — deletion should succeed
        ingredientService.deleteIngredient( created.getId() );
        assertThrows( Exception.class, () -> {
            ingredientService.getIngredientById( created.getId() );
        } );
    }

    /**
     * Fix #2: Deleting an ingredient that is referenced by an existing item must
     * throw IllegalArgumentException instead of silently removing it and leaving
     * the item in an inconsistent state.
     */
    @Test
    @Transactional
    public void testDeleteIngredient_UsedByItem_Throws () {
        // Create ingredient, then an item that depends on it
        final IngredientDto created = ingredientService.createIngredient( new IngredientDto( "ESPRESSO", 10 ) );

        ItemDto itemDto = new ItemDto();
        itemDto.setName( "Espresso Shot" );
        itemDto.setDescription( "Strong shot" );
        itemDto.setPrice( 2.50 );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 1 ) );
        itemService.addItem( itemDto );

        // Attempting to delete ESPRESSO now should be blocked
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
                () -> ingredientService.deleteIngredient( created.getId() ) );
        assertNotNull( ex.getMessage() );
        // Ingredient must still exist
        assertNotNull( ingredientService.getIngredientById( created.getId() ) );
    }

    /**
     * Fix #5: Deleting an ingredient must also remove its entry from the
     * inventory map so staff no longer see a stale ingredient in inventory.
     */
    @Test
    @Transactional
    public void testDeleteIngredient_RemovesFromInventory () {
        // Create ingredient — this also adds it to inventory
        final IngredientDto created = ingredientService.createIngredient( new IngredientDto( "MILK", 15 ) );

        // Confirm it's in inventory before deletion
        InventoryDto inventoryBefore = inventoryService.getInventory();
        assertNotNull( inventoryBefore.getIngredients().get( "MILK" ) );

        // Delete the ingredient (no items depend on it)
        ingredientService.deleteIngredient( created.getId() );

        // Confirm the inventory entry is gone
        InventoryDto inventoryAfter = inventoryService.getInventory();
        assertFalse( inventoryAfter.getIngredients().containsKey( "MILK" ),
                "Deleted ingredient should be removed from inventory map" );
    }

    /**
     * Deleting one ingredient should not affect other ingredients in inventory.
     */
    @Test
    @Transactional
    public void testDeleteIngredient_OtherIngredientsUnaffected () {
        ingredientService.createIngredient( new IngredientDto( "COFFEE", 10 ) );
        final IngredientDto milk = ingredientService.createIngredient( new IngredientDto( "MILK", 20 ) );

        // Delete only MILK
        ingredientService.deleteIngredient( milk.getId() );

        // COFFEE should still be present in inventory
        InventoryDto inventory = inventoryService.getInventory();
        assertNotNull( inventory.getIngredients().get( "COFFEE" ),
                "Other ingredients should remain in inventory after unrelated deletion" );
        assertFalse( inventory.getIngredients().containsKey( "MILK" ),
                "Deleted ingredient should not remain in inventory" );
    }

    /**
     * Tests that retrieving a non-existent ingredient throws an exception.
     */
    @Test
    @Transactional
    public void testGetIngredientById_NotFound () {
        assertThrows( Exception.class, () -> {
            ingredientService.getIngredientById( 999L );
        } );
    }

    /**
     * Tests that the IngredientMapper can be instantiated.
     */
    @Test
    @Transactional
    public void testIngredientMapper () {
        final IngredientMapper mapper = new IngredientMapper();
        assertNotNull( mapper );
    }

    /**
     * Tests that creating an ingredient with a null name throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_NullName () {
        final IngredientDto ingredient = new IngredientDto( null, 5 );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( ingredient );
        } );
    }

    /**
     * Tests that creating an ingredient with a null amount throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_NullAmount () {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", null );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( ingredient );
        } );
    }

    /**
     * Tests that creating an ingredient with a blank name throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testCreateIngredient_EmptyName () {
        final IngredientDto ingredient = new IngredientDto( "   ", 5 );
        assertThrows( IllegalArgumentException.class, () -> {
            ingredientService.createIngredient( ingredient );
        } );
    }
}
