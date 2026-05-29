package edu.ncsu.csc326.wolfcafe.service.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.OrderItem;

/**
 * Unit tests for InventoryUtils.
 */
public class InventoryUtilsTest {

    /** Inventory used across tests */
    private Inventory inventory;

    /**
     * Sets up a fresh inventory with known ingredient amounts before each test.
     */
    @BeforeEach
    public void setUp () {
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put( "ESPRESSO", 10 );
        ingredients.put( "MILK", 20 );
        ingredients.put( "SUGAR", 5 );
        inventory = new Inventory( 1L, ingredients );
    }

    // ── Helper builders ───────────────────────────────────────────────────────

    /**
     * Builds an Item with the given ingredient name/amount pairs.
     */
    private Item buildItem ( String... nameAmountPairs ) {
        Item item = new Item();
        for ( int i = 0; i < nameAmountPairs.length; i += 2 ) {
            item.addIngredient( new Ingredient( nameAmountPairs[i], Integer.parseInt( nameAmountPairs[i + 1] ) ) );
        }
        return item;
    }

    /**
     * Builds an OrderItem wrapping the given Item with the given quantity.
     */
    private OrderItem buildOrderItem ( Item item, int quantity ) {
        OrderItem oi = new OrderItem();
        oi.setItem( item );
        oi.setQuantity( quantity );
        return oi;
    }

    // ── hasEnoughForItem ──────────────────────────────────────────────────────

    /**
     * Returns true when inventory has exactly enough for one item.
     */
    @Test
    void testHasEnoughForItem_exactAmount () {
        Item item = buildItem( "ESPRESSO", "10" );
        assertTrue( InventoryUtils.hasEnoughForItem( inventory, item ) );
    }

    /**
     * Returns true when inventory has more than enough for one item.
     */
    @Test
    void testHasEnoughForItem_surplus () {
        Item item = buildItem( "ESPRESSO", "3", "MILK", "5" );
        assertTrue( InventoryUtils.hasEnoughForItem( inventory, item ) );
    }

    /**
     * Returns false when inventory has insufficient quantity of one ingredient.
     */
    @Test
    void testHasEnoughForItem_insufficient () {
        Item item = buildItem( "ESPRESSO", "11" ); // only 10 available
        assertFalse( InventoryUtils.hasEnoughForItem( inventory, item ) );
    }

    /**
     * Returns false when the item requires an ingredient not present in inventory.
     */
    @Test
    void testHasEnoughForItem_missingIngredient () {
        Item item = buildItem( "VANILLA", "1" ); // not in inventory
        assertFalse( InventoryUtils.hasEnoughForItem( inventory, item ) );
    }

    /**
     * Returns true when the item has no ingredients.
     */
    @Test
    void testHasEnoughForItem_noIngredients () {
        Item item = new Item(); // empty ingredient list
        assertTrue( InventoryUtils.hasEnoughForItem( inventory, item ) );
    }

    /**
     * Returns true when item is null.
     */
    @Test
    void testHasEnoughForItem_nullItem () {
        assertTrue( InventoryUtils.hasEnoughForItem( inventory, null ) );
    }

    // ── hasEnoughIngredients (overload with List<Ingredient>) ─────────────────

    /**
     * Returns true when inventory covers all ingredients in the list.
     */
    @Test
    void testHasEnoughIngredients_sufficient () {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add( new Ingredient( "ESPRESSO", 5 ) );
        ingredients.add( new Ingredient( "MILK", 10 ) );
        assertTrue( InventoryUtils.hasEnoughIngredients( inventory, ingredients ) );
    }

    /**
     * Returns false when one ingredient exceeds available stock.
     */
    @Test
    void testHasEnoughIngredients_insufficient () {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add( new Ingredient( "SUGAR", 6 ) ); // only 5 available
        assertFalse( InventoryUtils.hasEnoughIngredients( inventory, ingredients ) );
    }

    /**
     * Returns true for an empty ingredient list.
     */
    @Test
    void testHasEnoughIngredients_emptyList () {
        assertTrue( InventoryUtils.hasEnoughIngredients( inventory, new ArrayList<>() ) );
    }

    /**
     * Returns true for a null ingredient list.
     */
    @Test
    void testHasEnoughIngredients_nullList () {
        assertTrue( InventoryUtils.hasEnoughIngredients( inventory, null ) );
    }

    // ── hasEnoughForOrderItems ────────────────────────────────────────────────

    /**
     * Returns true when inventory covers a single order item (qty 1).
     */
    @Test
    void testHasEnoughForOrderItems_singleItemQty1 () {
        Item item = buildItem( "ESPRESSO", "2", "MILK", "3" );
        List<OrderItem> orderItems = List.of( buildOrderItem( item, 1 ) );
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, orderItems ) );
    }

    /**
     * Returns true when inventory covers a single order item with quantity > 1.
     */
    @Test
    void testHasEnoughForOrderItems_singleItemMultipleQty () {
        Item item = buildItem( "ESPRESSO", "2" ); // needs 2 per item
        List<OrderItem> orderItems = List.of( buildOrderItem( item, 4 ) ); // total: 8, available: 10
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, orderItems ) );
    }

    /**
     * Returns false when quantity pushes total required above available stock.
     */
    @Test
    void testHasEnoughForOrderItems_insufficientQty () {
        Item item = buildItem( "ESPRESSO", "3" ); // needs 3 per item
        List<OrderItem> orderItems = List.of( buildOrderItem( item, 4 ) ); // total: 12, available: 10
        assertFalse( InventoryUtils.hasEnoughForOrderItems( inventory, orderItems ) );
    }

    /**
     * Returns true when multiple order items together fit within inventory.
     */
    @Test
    void testHasEnoughForOrderItems_multipleItemsSufficient () {
        Item coffee = buildItem( "ESPRESSO", "2", "MILK", "3" );
        Item latte  = buildItem( "ESPRESSO", "3", "MILK", "5" );
        // Total ESPRESSO: 2+3=5 (have 10), MILK: 3+5=8 (have 20)
        List<OrderItem> orderItems = List.of(
                buildOrderItem( coffee, 1 ),
                buildOrderItem( latte, 1 ) );
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, orderItems ) );
    }

    /**
     * Returns false when multiple order items together exceed inventory for one ingredient.
     */
    @Test
    void testHasEnoughForOrderItems_multipleItemsInsufficient () {
        Item coffee = buildItem( "SUGAR", "3" );
        Item latte  = buildItem( "SUGAR", "3" );
        // Total SUGAR: 3+3=6, but only 5 available
        List<OrderItem> orderItems = List.of(
                buildOrderItem( coffee, 1 ),
                buildOrderItem( latte, 1 ) );
        assertFalse( InventoryUtils.hasEnoughForOrderItems( inventory, orderItems ) );
    }

    /**
     * Returns true for an empty order items list.
     */
    @Test
    void testHasEnoughForOrderItems_emptyList () {
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, new ArrayList<>() ) );
    }

    /**
     * Returns true for a null order items list.
     */
    @Test
    void testHasEnoughForOrderItems_nullList () {
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, null ) );
    }

    /**
     * Skips an order item whose item reference is null without throwing.
     */
    @Test
    void testHasEnoughForOrderItems_nullItemReference () {
        OrderItem oi = buildOrderItem( null, 1 );
        assertTrue( InventoryUtils.hasEnoughForOrderItems( inventory, List.of( oi ) ) );
    }

    // ── deductOrderItemsFromInventory ─────────────────────────────────────────

    /**
     * Correctly deducts ingredient amounts for a single order item (qty 1).
     */
    @Test
    void testDeductOrderItemsFromInventory_singleItem () {
        Item item = buildItem( "ESPRESSO", "3", "MILK", "5" );
        InventoryUtils.deductOrderItemsFromInventory( inventory, List.of( buildOrderItem( item, 1 ) ) );

        assertEquals( 7, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 15, inventory.getIngredients().get( "MILK" ) );
        assertEquals( 5, inventory.getIngredients().get( "SUGAR" ) ); // unchanged
    }

    /**
     * Correctly deducts ingredient amounts scaled by quantity > 1.
     */
    @Test
    void testDeductOrderItemsFromInventory_multipleQty () {
        Item item = buildItem( "ESPRESSO", "2" );
        InventoryUtils.deductOrderItemsFromInventory( inventory, List.of( buildOrderItem( item, 3 ) ) );
        // 3 * 2 = 6 deducted from 10
        assertEquals( 4, inventory.getIngredients().get( "ESPRESSO" ) );
    }

    /**
     * Correctly deducts across multiple different order items.
     */
    @Test
    void testDeductOrderItemsFromInventory_multipleItems () {
        Item coffee = buildItem( "ESPRESSO", "2" );
        Item latte  = buildItem( "ESPRESSO", "3", "MILK", "4" );
        InventoryUtils.deductOrderItemsFromInventory( inventory,
                List.of( buildOrderItem( coffee, 1 ), buildOrderItem( latte, 2 ) ) );
        // ESPRESSO: 10 - 2 - (3*2)=6 → 2
        // MILK:     20 - (4*2)=8     → 12
        assertEquals( 2, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 12, inventory.getIngredients().get( "MILK" ) );
    }

    /**
     * Does nothing when passed an empty list.
     */
    @Test
    void testDeductOrderItemsFromInventory_emptyList () {
        InventoryUtils.deductOrderItemsFromInventory( inventory, new ArrayList<>() );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 20, inventory.getIngredients().get( "MILK" ) );
        assertEquals( 5, inventory.getIngredients().get( "SUGAR" ) );
    }

    /**
     * Does nothing when passed a null list.
     */
    @Test
    void testDeductOrderItemsFromInventory_nullList () {
        InventoryUtils.deductOrderItemsFromInventory( inventory, null );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
    }

    /**
     * Skips order items with a null item reference without throwing.
     */
    @Test
    void testDeductOrderItemsFromInventory_nullItemReference () {
        OrderItem oi = buildOrderItem( null, 1 );
        assertDoesNotThrow( () -> InventoryUtils.deductOrderItemsFromInventory( inventory, List.of( oi ) ) );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) ); // unchanged
    }

    // ── restoreOrderItemsToInventory ──────────────────────────────────────────

    /**
     * Correctly restores ingredient amounts for a single order item (qty 1).
     * Simulates cancelling a fulfilled order: deduct first, then restore.
     */
    @Test
    void testRestoreOrderItemsToInventory_singleItem () {
        Item item = buildItem( "ESPRESSO", "3", "MILK", "5" );
        List<OrderItem> orderItems = List.of( buildOrderItem( item, 1 ) );

        // Simulate prior fulfillment deduction
        InventoryUtils.deductOrderItemsFromInventory( inventory, orderItems );
        assertEquals( 7, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 15, inventory.getIngredients().get( "MILK" ) );

        // Restore on cancellation
        InventoryUtils.restoreOrderItemsToInventory( inventory, orderItems );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 20, inventory.getIngredients().get( "MILK" ) );
        assertEquals( 5, inventory.getIngredients().get( "SUGAR" ) ); // unchanged throughout
    }

    /**
     * Correctly restores ingredient amounts scaled by quantity > 1.
     */
    @Test
    void testRestoreOrderItemsToInventory_multipleQty () {
        Item item = buildItem( "ESPRESSO", "2" );
        List<OrderItem> orderItems = List.of( buildOrderItem( item, 3 ) );

        // Deduct: 10 - (2*3)=6 → 4
        InventoryUtils.deductOrderItemsFromInventory( inventory, orderItems );
        assertEquals( 4, inventory.getIngredients().get( "ESPRESSO" ) );

        // Restore: 4 + (2*3)=6 → 10
        InventoryUtils.restoreOrderItemsToInventory( inventory, orderItems );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
    }

    /**
     * Correctly restores across multiple different order items.
     */
    @Test
    void testRestoreOrderItemsToInventory_multipleItems () {
        Item coffee = buildItem( "ESPRESSO", "2" );
        Item latte  = buildItem( "ESPRESSO", "3", "MILK", "4" );
        List<OrderItem> orderItems = List.of(
                buildOrderItem( coffee, 1 ),
                buildOrderItem( latte, 2 ) );

        InventoryUtils.deductOrderItemsFromInventory( inventory, orderItems );
        // ESPRESSO: 10 - 2 - 6 = 2, MILK: 20 - 8 = 12
        assertEquals( 2, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 12, inventory.getIngredients().get( "MILK" ) );

        InventoryUtils.restoreOrderItemsToInventory( inventory, orderItems );
        // All values restored to original
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 20, inventory.getIngredients().get( "MILK" ) );
    }

    /**
     * Restore adds ingredients back even when the inventory map has no existing
     * entry for that ingredient (e.g. it was previously zeroed out and removed).
     */
    @Test
    void testRestoreOrderItemsToInventory_ingredientAbsentFromMap () {
        // Remove SUGAR entirely from the inventory map
        inventory.getIngredients().remove( "SUGAR" );

        Item item = buildItem( "SUGAR", "3" );
        InventoryUtils.restoreOrderItemsToInventory( inventory, List.of( buildOrderItem( item, 1 ) ) );

        // Should add 3 back using getOrDefault(0)
        assertEquals( 3, inventory.getIngredients().get( "SUGAR" ) );
    }

    /**
     * Does nothing when passed an empty list.
     */
    @Test
    void testRestoreOrderItemsToInventory_emptyList () {
        InventoryUtils.restoreOrderItemsToInventory( inventory, new ArrayList<>() );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 20, inventory.getIngredients().get( "MILK" ) );
        assertEquals( 5, inventory.getIngredients().get( "SUGAR" ) );
    }

    /**
     * Does nothing when passed a null list.
     */
    @Test
    void testRestoreOrderItemsToInventory_nullList () {
        InventoryUtils.restoreOrderItemsToInventory( inventory, null );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
    }

    /**
     * Skips order items with a null item reference without throwing.
     */
    @Test
    void testRestoreOrderItemsToInventory_nullItemReference () {
        OrderItem oi = buildOrderItem( null, 1 );
        assertDoesNotThrow( () -> InventoryUtils.restoreOrderItemsToInventory( inventory, List.of( oi ) ) );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) ); // unchanged
    }

    // ── deductIngredients (List<Ingredient> overload) ─────────────────────────

    /**
     * Correctly deducts a list of ingredients directly from inventory.
     */
    @Test
    void testDeductIngredients_basic () {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add( new Ingredient( "ESPRESSO", 4 ) );
        ingredients.add( new Ingredient( "SUGAR", 2 ) );
        InventoryUtils.deductIngredients( inventory, ingredients );

        assertEquals( 6, inventory.getIngredients().get( "ESPRESSO" ) );
        assertEquals( 3, inventory.getIngredients().get( "SUGAR" ) );
        assertEquals( 20, inventory.getIngredients().get( "MILK" ) ); // unchanged
    }

    /**
     * Does nothing when passed an empty list.
     */
    @Test
    void testDeductIngredients_emptyList () {
        InventoryUtils.deductIngredients( inventory, new ArrayList<>() );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
    }

    /**
     * Does nothing when passed a null list.
     */
    @Test
    void testDeductIngredients_nullList () {
        InventoryUtils.deductIngredients( inventory, null );
        assertEquals( 10, inventory.getIngredients().get( "ESPRESSO" ) );
    }
}