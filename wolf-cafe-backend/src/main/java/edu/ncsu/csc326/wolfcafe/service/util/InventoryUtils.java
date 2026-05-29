package edu.ncsu.csc326.wolfcafe.service.util;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * Utility class for inventory validation and adjustment operations.
 * Provides common logic for checking ingredient availability and deducting from inventory.
 */
public class InventoryUtils {

    /**
     * Checks if the inventory has enough ingredients to make a single item.
     *
     * @param inventory the current inventory
     * @param item the item to check
     * @return true if inventory has enough of all required ingredients
     */
    public static boolean hasEnoughForItem(Inventory inventory, Item item) {
        if (item == null || item.getIngredients() == null || item.getIngredients().isEmpty()) {
            return true;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for (Ingredient ingredient : item.getIngredients()) {
            String ingredientName = ingredient.getName();
            Integer requiredAmount = ingredient.getAmount();

            Integer availableAmount = inventoryMap.getOrDefault(ingredientName, 0);

            if (availableAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the inventory has enough ingredients to fulfill all items in a list of order items.
     *
     * @param inventory the current inventory
     * @param orderItems the order items to check
     * @return true if inventory has enough of all required ingredients for all items
     */
    public static boolean hasEnoughForOrderItems(Inventory inventory, List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return true;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();
        
        // Create a copy to simulate deductions without modifying the original
        Map<String, Integer> simulatedInventory = new java.util.HashMap<>(inventoryMap);

        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            int quantity = orderItem.getQuantity();

            if (item == null || item.getIngredients() == null) {
                continue;
            }

            // Check if we have enough for this quantity
            for (Ingredient ingredient : item.getIngredients()) {
                String ingredientName = ingredient.getName();
                Integer requiredPerItem = ingredient.getAmount();
                Integer totalRequired = requiredPerItem * quantity;

                Integer availableAmount = simulatedInventory.getOrDefault(ingredientName, 0);

                if (availableAmount < totalRequired) {
                    return false;
                }

                // Simulate deduction
                simulatedInventory.put(ingredientName, availableAmount - totalRequired);
            }
        }

        return true;
    }

    /**
     * Deducts the ingredients required for all order items from the inventory.
     * Assumes sufficient inventory has already been validated.
     *
     * @param inventory the inventory to update
     * @param orderItems the items being fulfilled
     */
    public static void deductOrderItemsFromInventory(Inventory inventory, List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            int quantity = orderItem.getQuantity();

            if (item == null || item.getIngredients() == null) {
                continue;
            }

            for (Ingredient ingredient : item.getIngredients()) {
                String ingredientName = ingredient.getName();
                Integer requiredPerItem = ingredient.getAmount();
                Integer totalRequired = requiredPerItem * quantity;

                Integer availableAmount = inventoryMap.getOrDefault(ingredientName, 0);

                // Deduct the ingredient amount required from available
                inventoryMap.put(ingredientName, availableAmount - totalRequired);
            }
        }
    }

    /**
     * Checks if there are enough ingredients to make a single item.
     * This is an overloaded method for direct Ingredient list checking.
     *
     * @param inventory the current inventory
     * @param ingredients the ingredients to check
     * @return true if inventory has enough of all required ingredients
     */
    public static boolean hasEnoughIngredients(Inventory inventory, List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return true;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for (Ingredient ingredient : ingredients) {
            String ingredientName = ingredient.getName();
            Integer requiredAmount = ingredient.getAmount();

            Integer availableAmount = inventoryMap.getOrDefault(ingredientName, 0);

            if (availableAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    /**
     * Restores the ingredients consumed by a fulfilled order back into inventory.
     * Should be called when a READY_FOR_PICKUP order is cancelled, since inventory
     * was already deducted at fulfillment time.
     *
     * @param inventory  the inventory to update
     * @param orderItems the order items whose ingredients should be restored
     */
    public static void restoreOrderItemsToInventory(Inventory inventory, List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            int quantity = orderItem.getQuantity();

            if (item == null || item.getIngredients() == null) {
                continue;
            }

            for (Ingredient ingredient : item.getIngredients()) {
                String ingredientName = ingredient.getName();
                Integer restoredPerItem = ingredient.getAmount();
                Integer totalRestored = restoredPerItem * quantity;

                Integer availableAmount = inventoryMap.getOrDefault(ingredientName, 0);

                // Add the ingredient amount back to inventory
                inventoryMap.put(ingredientName, availableAmount + totalRestored);
            }
        }
    }

    /**
     * Deducts ingredients from the inventory.
     *
     * @param inventory the inventory to update
     * @param ingredients the ingredients to deduct
     */
    public static void deductIngredients(Inventory inventory, List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for (Ingredient ingredient : ingredients) {
            String ingredientName = ingredient.getName();
            Integer requiredAmount = ingredient.getAmount();

            Integer availableAmount = inventoryMap.getOrDefault(ingredientName, 0);

            // Deduct the ingredient amount required from available
            inventoryMap.put(ingredientName, availableAmount - requiredAmount);
        }
    }
}
