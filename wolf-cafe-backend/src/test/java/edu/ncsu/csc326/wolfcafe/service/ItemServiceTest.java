package edu.ncsu.csc326.wolfcafe.service;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.util.List;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
 
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
 
/**
 * Tests ItemServiceImpl.
 */
@SpringBootTest
public class ItemServiceTest {
 
    /** Reference to ItemService */
    @Autowired
    private ItemService itemService;

    /** Reference to IngredientService */
    @Autowired
    private IngredientService ingredientService;

    /** Reference to InventoryService */
    @Autowired
    private InventoryService inventoryService;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;
 
    /** Item name */
    private static final String ITEM_NAME        = "Coffee";
    /** Item description */
    private static final String ITEM_DESCRIPTION = "Coffee is life";
    /** Item price */
    private static final double ITEM_PRICE       = 3.25;
 
    /**
     * Clears relevant tables before each test so tests are fully isolated.
     */
    @BeforeEach
    public void setUp () throws Exception {
        entityManager.createNativeQuery( "DELETE FROM order_items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM orders" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items_ingredients" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory_ingredients" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM ingredient" ).executeUpdate();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Creates an ingredient via IngredientService so it lands in inventory too.
     *
     * @param name   ingredient name (will be stored uppercased)
     * @param amount initial inventory amount
     * @return the created IngredientDto
     */
    private IngredientDto createIngredient ( String name, int amount ) {
        IngredientDto dto = new IngredientDto();
        dto.setName( name );
        dto.setAmount( amount );
        return ingredientService.createIngredient( dto );
    }

    /**
     * Builds a minimal valid ItemDto with no ingredients.
     */
    private ItemDto buildItemDto ( String name, String description, double price ) {
        ItemDto dto = new ItemDto();
        dto.setName( name );
        dto.setDescription( description );
        dto.setPrice( price );
        return dto;
    }

    // ── Create (no ingredients) ───────────────────────────────────────────────

    /**
     * Tests that a valid item without ingredients is created and all fields
     * are persisted correctly.
     */
    @Test
    @Transactional
    void testCreateItem () {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );

        ItemDto created = itemService.addItem( itemDto );
        assertAll( "Created item fields",
                () -> assertNotNull( created.getId() ),
                () -> assertEquals( ITEM_NAME, created.getName() ),
                () -> assertEquals( ITEM_DESCRIPTION, created.getDescription() ),
                () -> assertEquals( ITEM_PRICE, created.getPrice() ) );
    }
 
    /**
     * Tests that leading/trailing whitespace in the name is trimmed on create.
     */
    @Test
    @Transactional
    void testCreateItemNameTrimmed () {
        ItemDto itemDto = buildItemDto( "  Coffee  ", ITEM_DESCRIPTION, ITEM_PRICE );

        ItemDto created = itemService.addItem( itemDto );
        assertEquals( "Coffee", created.getName() );
    }
 
    /**
     * UC3 Alt: [Missing Required Fields] — null name throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemNullNameThrows () {
        ItemDto itemDto = buildItemDto( null, ITEM_DESCRIPTION, ITEM_PRICE );
        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }
 
    /**
     * UC3 Alt: [Missing Required Fields] — blank name throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemBlankNameThrows () {
        ItemDto itemDto = buildItemDto( "   ", ITEM_DESCRIPTION, ITEM_PRICE );
        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }
 
    /**
     * UC3 Alt: [Invalid Price] — negative price throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemNegativePriceThrows () {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, -1.0 );
        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }
 
    /**
     * UC3 Alt: [Duplicate Item Name] — adding a second item with the same name
     * throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemDuplicateNameThrows () {
        itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.addItem( buildItemDto( ITEM_NAME, "Another description", 4.00 ) ) );
    }
 
    /**
     * Tests that a price of zero is accepted (boundary: not negative).
     */
    @Test
    @Transactional
    void testCreateItemZeroPriceAccepted () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, 0.0 ) );
        assertEquals( 0.0, created.getPrice() );
    }

    // ── Create with ingredients ───────────────────────────────────────────────

    /**
     * Tests that an item can be created with valid ingredients that exist in
     * inventory, and the ingredient list is persisted on the item.
     */
    @Test
    @Transactional
    void testCreateItemWithValidIngredients () {
        createIngredient( "ESPRESSO", 10 );
        createIngredient( "MILK", 20 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        itemDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );

        ItemDto created = itemService.addItem( itemDto );
        assertNotNull( created.getId() );
        assertEquals( ITEM_NAME, created.getName() );
    }

    /**
     * Tests that ingredient names are normalised to uppercase when stored.
     */
    @Test
    @Transactional
    void testCreateItemIngredientNamesUppercased () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "espresso", 2 ) );   // lowercase input

        // Should not throw — "espresso" uppercased matches "ESPRESSO" in inventory
        ItemDto created = itemService.addItem( itemDto );
        assertNotNull( created.getId() );
    }

    /**
     * Tests that creating an item with an ingredient that does NOT exist in
     * inventory throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemIngredientNotInInventoryThrows () {
        // No ingredients added to inventory — ESPRESSO doesn't exist
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    /**
     * Tests that if one ingredient is valid but another is missing from inventory,
     * the whole create fails.
     */
    @Test
    @Transactional
    void testCreateItemOneIngredientMissingThrows () {
        createIngredient( "ESPRESSO", 10 );
        // MILK deliberately not added

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        itemDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    /**
     * Tests that providing an ingredient with a null name throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemIngredientNullNameThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( null, 2 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    /**
     * Tests that providing an ingredient with amount zero throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemIngredientZeroAmountThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 0 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    /**
     * Tests that providing an ingredient with a negative amount throws
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemIngredientNegativeAmountThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", -3 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    /**
     * Tests that an item with an empty ingredient list (explicitly set) is
     * accepted — ingredients are optional.
     */
    @Test
    @Transactional
    void testCreateItemEmptyIngredientListAccepted () {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        // ingredients list is empty by default — no inventory needed

        ItemDto created = itemService.addItem( itemDto );
        assertNotNull( created.getId() );
    }

    /**
     * Tests that attempting to create an item with ingredients when no inventory
     * exists at all throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testCreateItemNoInventoryExistsThrows () {
        // inventory table is empty (cleared in setUp)
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );

        assertThrows( IllegalArgumentException.class, () -> itemService.addItem( itemDto ) );
    }

    // ── Get ───────────────────────────────────────────────────────────────────
 
    /**
     * Tests that an item can be retrieved by its id after creation.
     */
    @Test
    @Transactional
    void testGetItem () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        ItemDto retrieved = itemService.getItem( created.getId() );

        assertAll( "Retrieved item fields",
                () -> assertEquals( ITEM_NAME, retrieved.getName() ),
                () -> assertEquals( ITEM_DESCRIPTION, retrieved.getDescription() ),
                () -> assertEquals( ITEM_PRICE, retrieved.getPrice() ) );
    }
 
    /**
     * Tests that fetching a non-existent id throws ResourceNotFoundException.
     */
    @Test
    @Transactional
    void testGetItemNotFoundThrows () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        assertThrows( ResourceNotFoundException.class,
                () -> itemService.getItem( created.getId() + 1 ) );
    }
 
    /**
     * Tests that getAllItems returns an empty list when no items exist.
     */
    @Test
    @Transactional
    void testGetAllItemsEmpty () {
        List<ItemDto> items = itemService.getAllItems();
        assertTrue( items.isEmpty() );
    }
 
    /**
     * Tests that getAllItems returns all persisted items.
     */
    @Test
    @Transactional
    void testGetAllItems () {
        itemService.addItem( buildItemDto( "Coffee", "desc a", 1.0 ) );
        itemService.addItem( buildItemDto( "Latte", "desc b", 2.0 ) );

        List<ItemDto> items = itemService.getAllItems();
        assertEquals( 2, items.size() );
    }

    // ── Update (basic fields) ─────────────────────────────────────────────────
 
    /**
     * Tests that all fields of an item are updated correctly.
     */
    @Test
    @Transactional
    void testUpdateItem () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        ItemDto update = buildItemDto( "Latte", "A yummy beverage", 3.57 );
        ItemDto updated = itemService.updateItem( created.getId(), update );

        assertAll( "Updated item fields",
                () -> assertEquals( "Latte", updated.getName() ),
                () -> assertEquals( "A yummy beverage", updated.getDescription() ),
                () -> assertEquals( 3.57, updated.getPrice() ) );
    }
 
    /**
     * Tests that updating an item with its own existing name (no change) succeeds —
     * the duplicate check must not fire for the item's own name.
     */
    @Test
    @Transactional
    void testUpdateItemSameNameAllowed () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        ItemDto update = buildItemDto( ITEM_NAME, "Updated description", 5.00 );
        ItemDto updated = itemService.updateItem( created.getId(), update );
        assertEquals( ITEM_NAME, updated.getName() );
    }
 
    /**
     * Tests that name whitespace is trimmed on update.
     */
    @Test
    @Transactional
    void testUpdateItemNameTrimmed () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        ItemDto update = buildItemDto( "  Espresso  ", ITEM_DESCRIPTION, ITEM_PRICE );
        ItemDto updated = itemService.updateItem( created.getId(), update );
        assertEquals( "Espresso", updated.getName() );
    }
 
    /**
     * Tests that updating a non-existent id throws ResourceNotFoundException.
     */
    @Test
    @Transactional
    void testUpdateItemNotFoundThrows () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        assertThrows( ResourceNotFoundException.class,
                () -> itemService.updateItem( created.getId() + 1, buildItemDto( "Latte", "desc", 3.00 ) ) );
    }
 
    /**
     * UC3 Alt: [Missing Required Fields] — blank name on update throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testUpdateItemBlankNameThrows () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), buildItemDto( "", "desc", 3.00 ) ) );
    }
 
    /**
     * UC3 Alt: [Invalid Price] — negative price on update throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testUpdateItemNegativePriceThrows () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), buildItemDto( "Latte", "desc", -5.00 ) ) );
    }
 
    /**
     * UC3 Alt: [Duplicate Item Name] — updating an item to use another item's name
     * throws IllegalArgumentException.
     */
    @Test
    @Transactional
    void testUpdateItemDuplicateNameThrows () {
        itemService.addItem( buildItemDto( "Coffee", "desc", 2.00 ) );
        ItemDto createdB = itemService.addItem( buildItemDto( "Latte", "desc", 3.00 ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( createdB.getId(), buildItemDto( "Coffee", "desc", 3.00 ) ) );
    }

    // ── Update with ingredients (Fix #3) ─────────────────────────────────────

    /**
     * Fix #3: Updating an item with valid ingredients that exist in inventory
     * should succeed and persist the updated ingredient list.
     */
    @Test
    @Transactional
    void testUpdateItemWithValidIngredients () {
        createIngredient( "ESPRESSO", 10 );
        createIngredient( "MILK", 20 );

        // Create item initially with ESPRESSO only
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        ItemDto created = itemService.addItem( itemDto );

        // Update to use ESPRESSO + MILK
        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        update.getIngredients().add( new IngredientDto( "ESPRESSO", 1 ) );
        update.getIngredients().add( new IngredientDto( "MILK", 3 ) );

        ItemDto updated = itemService.updateItem( created.getId(), update );
        assertNotNull( updated.getId() );
        assertEquals( ITEM_NAME, updated.getName() );
    }

    /**
     * Fix #3: Updating an item to use an ingredient that does NOT exist in
     * inventory must throw IllegalArgumentException — same guard as addItem.
     */
    @Test
    @Transactional
    void testUpdateItemIngredientNotInInventoryThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        ItemDto created = itemService.addItem( itemDto );

        // Try to update using VANILLA which was never added to inventory
        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        update.getIngredients().add( new IngredientDto( "VANILLA", 1 ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), update ) );
    }

    /**
     * Fix #3: Updating an item with ingredients when no inventory exists must
     * throw IllegalArgumentException.
     */
    @Test
    @Transactional
    void testUpdateItemNoInventoryExistsThrows () {
        // Create item first without ingredients (no inventory needed)
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        // Now try to add an ingredient on update with empty inventory
        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        update.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), update ) );
    }

    /**
     * Fix #3: Updating an item to remove all ingredients (empty list) should
     * succeed — an item with no ingredients is valid.
     */
    @Test
    @Transactional
    void testUpdateItemRemoveAllIngredients () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        ItemDto created = itemService.addItem( itemDto );

        // Update with no ingredients — empty list is valid
        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        // ingredients list is empty by default

        assertDoesNotThrow( () -> itemService.updateItem( created.getId(), update ) );
    }

    /**
     * Fix #3: Updating an item with an ingredient of zero amount must throw
     * IllegalArgumentException — same amount validation as addItem.
     */
    @Test
    @Transactional
    void testUpdateItemIngredientZeroAmountThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        update.getIngredients().add( new IngredientDto( "ESPRESSO", 0 ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), update ) );
    }

    /**
     * Fix #3: Updating an item with a null ingredient name must throw
     * IllegalArgumentException.
     */
    @Test
    @Transactional
    void testUpdateItemIngredientNullNameThrows () {
        createIngredient( "ESPRESSO", 10 );

        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );

        ItemDto update = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        update.getIngredients().add( new IngredientDto( null, 2 ) );

        assertThrows( IllegalArgumentException.class,
                () -> itemService.updateItem( created.getId(), update ) );
    }

    // ── Delete ────────────────────────────────────────────────────────────────
 
    /**
     * Tests that a deleted item can no longer be retrieved.
     */
    @Test
    @Transactional
    void testDeleteItem () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        itemService.deleteItem( created.getId() );
        assertThrows( ResourceNotFoundException.class, () -> itemService.getItem( created.getId() ) );
    }
 
    /**
     * Tests that deleting a non-existent id throws ResourceNotFoundException.
     */
    @Test
    @Transactional
    void testDeleteItemNotFoundThrows () {
        ItemDto created = itemService.addItem( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) );
        assertThrows( ResourceNotFoundException.class,
                () -> itemService.deleteItem( created.getId() + 1 ) );
    }
}