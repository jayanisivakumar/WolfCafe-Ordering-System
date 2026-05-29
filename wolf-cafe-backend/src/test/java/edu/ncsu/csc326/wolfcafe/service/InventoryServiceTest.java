/**
 * Test class of the InventoryService of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.service;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.util.HashMap;
import java.util.Map;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
 
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import jakarta.persistence.EntityManager;
 
/**
 * Tests InventoryServiceImpl.
 */
@SpringBootTest
public class InventoryServiceTest {
 
    /**
     * Default constructor for InventoryServiceTest.
     */
    public InventoryServiceTest() {
    }
 
    /** Reference to InventoryService (and InventoryServiceImpl). */
    @Autowired
    private InventoryService inventoryService;
 
    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;
 
    /**
     * Clears the inventory before each test to ensure a clean singleton state.
     *
     * @throws Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        entityManager.createNativeQuery( "DELETE FROM inventory_ingredients" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();
    }
 
    /**
     * Tests that createInventory persists all ingredient entries correctly.
     */
    @Test
    @Transactional
    public void testCreateInventory() {
        final Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put( "Vanilla", 5 );
        ingredients.put( "OatMilk", 9 );
        ingredients.put( "Matcha", 14 );
        ingredients.put( "PumpkinSpice", 23 );
 
        final InventoryDto inventoryDto = new InventoryDto();
        inventoryDto.setIngredients( ingredients );
 
        final InventoryDto created = inventoryService.createInventory( inventoryDto );
        assertAll( "Created inventory contents",
                () -> assertNotNull( created.getId() ),
                () -> assertEquals( 5, created.getIngredients().get( "Vanilla" ) ),
                () -> assertEquals( 9, created.getIngredients().get( "OatMilk" ) ),
                () -> assertEquals( 14, created.getIngredients().get( "Matcha" ) ),
                () -> assertEquals( 23, created.getIngredients().get( "PumpkinSpice" ) ) );
    }
 
    /**
     * UC3 Steps 13-14: getInventory returns the existing singleton inventory.
     */
    @Test
    @Transactional
    public void testGetInventoryReturnsExisting() {
        // Seed one inventory row
        final Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put( "Vanilla", 10 );
        final InventoryDto seed = new InventoryDto();
        seed.setIngredients( ingredients );
        inventoryService.createInventory( seed );
 
        final InventoryDto fetched = inventoryService.getInventory();
        assertEquals( 10, fetched.getIngredients().get( "Vanilla" ) );
    }
 
    /**
     * getInventory auto-creates an empty inventory when none exists.
     */
    @Test
    @Transactional
    public void testGetInventoryCreatesEmptyWhenNoneExists() {
        final InventoryDto fetched = inventoryService.getInventory();
        assertNotNull( fetched );
        assertNotNull( fetched.getId() );
        assertTrue( fetched.getIngredients().isEmpty() );
    }
 
    /**
     * UC3 Steps 15-17: updateInventory adds the supplied amounts to current levels.
     * Starting from zero, the result should equal the supplied values.
     */
    @Test
    @Transactional
    public void testUpdateInventoryAddsToExistingValues() {
        // Start with an existing inventory
        inventoryService.getInventory();
 
        final Map<String, Integer> toAdd = new HashMap<>();
        toAdd.put( "Vanilla", 35 );
        toAdd.put( "OatMilk", 17 );
        toAdd.put( "Matcha", 12 );
        toAdd.put( "PumpkinSpice", 14 );
 
        final InventoryDto inventoryDto = new InventoryDto();
        inventoryDto.setIngredients( toAdd );
 
        final InventoryDto updated = inventoryService.updateInventory( inventoryDto );
        assertAll( "Updated inventory contents",
                () -> assertEquals( 35, updated.getIngredients().get( "Vanilla" ) ),
                () -> assertEquals( 17, updated.getIngredients().get( "OatMilk" ) ),
                () -> assertEquals( 12, updated.getIngredients().get( "Matcha" ) ),
                () -> assertEquals( 14, updated.getIngredients().get( "PumpkinSpice" ) ) );
    }
 
    /**
     * Tests that updateInventory is additive — calling it twice accumulates quantities.
     */
    @Test
    @Transactional
    public void testUpdateInventoryIsAdditive() {
        inventoryService.getInventory();
 
        final Map<String, Integer> first = new HashMap<>();
        first.put( "Vanilla", 10 );
        final InventoryDto dto1 = new InventoryDto();
        dto1.setIngredients( first );
        inventoryService.updateInventory( dto1 );
 
        final Map<String, Integer> second = new HashMap<>();
        second.put( "Vanilla", 5 );
        final InventoryDto dto2 = new InventoryDto();
        dto2.setIngredients( second );
        final InventoryDto result = inventoryService.updateInventory( dto2 );
 
        assertEquals( 15, result.getIngredients().get( "Vanilla" ) );
    }
 
    /**
     * UC3 Alt: [Invalid Inventory Quantity] — negative quantity throws IllegalArgumentException.
     */
    @Test
    @Transactional
    public void testUpdateInventoryNegativeQuantityThrows() {
        inventoryService.getInventory();
 
        final Map<String, Integer> bad = new HashMap<>();
        bad.put( "Vanilla", -5 );
        final InventoryDto dto = new InventoryDto();
        dto.setIngredients( bad );
 
        assertThrows( IllegalArgumentException.class,
                () -> inventoryService.updateInventory( dto ) );
    }
 
    /**
     * UC3 Alt: [Invalid Inventory Quantity] — zero quantity throws IllegalArgumentException
     * (only positive integers are valid).
     */
    @Test
    @Transactional
    public void testUpdateInventoryZeroQuantityThrows() {
        inventoryService.getInventory();
 
        final Map<String, Integer> bad = new HashMap<>();
        bad.put( "Matcha", 0 );
        final InventoryDto dto = new InventoryDto();
        dto.setIngredients( bad );
 
        assertThrows( IllegalArgumentException.class,
                () -> inventoryService.updateInventory( dto ) );
    }
 
    /**
     * validateQuantity returns true for a positive integer.
     */
    @Test
    @Transactional
    public void testValidateQuantityPositive() {
        assertTrue( inventoryService.validateQuantity( 1 ) );
        assertTrue( inventoryService.validateQuantity( 100 ) );
    }
 
    /**
     * validateQuantity returns false for zero.
     */
    @Test
    @Transactional
    public void testValidateQuantityZero() {
        assertFalse( inventoryService.validateQuantity( 0 ) );
    }
 
    /**
     * validateQuantity returns false for a negative value.
     */
    @Test
    @Transactional
    public void testValidateQuantityNegative() {
        assertFalse( inventoryService.validateQuantity( -1 ) );
        assertFalse( inventoryService.validateQuantity( -999 ) );
    }
}
