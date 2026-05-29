/**
 * Represents the MakeRecipe Service of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.mapper.RecipeMapper;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.service.MakeRecipeService;

/**
 * Implementation of the MakeRecipeService interface.
 */
@Service
public class MakeRecipeServiceImpl implements MakeRecipeService {

    /**
     * Default constructor
     *
     */
    public MakeRecipeServiceImpl () {
    }

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Removes the ingredients used to make the specified recipe. Assumes that
     * the user has checked that there are enough ingredients to make
     *
     * @param inventoryDto
     *            current inventory
     * @param recipeDto
     *            recipe to make
     * @return updated inventory
     */
    @Override
    public boolean makeRecipe ( InventoryDto inventoryDto, RecipeDto recipeDto ) {
        Inventory inventory = InventoryMapper.mapToInventory( inventoryDto );
        Recipe recipe = RecipeMapper.mapToRecipe( recipeDto );

        // first let's check if there's enough ingredients before modifying
        // inventory
        if ( !enoughIngredients( inventory, recipe ) ) {
            return false;
        }

        // Deduct required ingredient amounts from inventory
        deductIngredients( inventory, recipe );

        inventoryRepository.save( inventory );

        return true;
    }

    /**
     * Returns true if there are enough ingredients to make the beverage.
     *
     * @param inventory
     *            coffee maker inventory
     * @param recipe
     *            recipe to check if there are enough ingredients
     * @return true if enough ingredients to make the beverage
     */
    private boolean enoughIngredients ( Inventory inventory, Recipe recipe ) {
        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for ( Ingredient ingredient : recipe.getIngredients() ) {

            String ingredientName = ingredient.getName();
            Integer requiredAmount = ingredient.getAmount();

            Integer availableAmount = inventoryMap.getOrDefault( ingredientName, 0 );

            // if ingredient insufficient it fails immediately
            if ( availableAmount < requiredAmount ) {
                return false;
            }
        }

        return true;
    }

    /**
     * Deducts required ingredients from inventory. Assumes sufficient inventory
     * already validated.
     *
     * @param inventory
     *            inventory to update
     * @param recipe
     *            recipe being made
     */
    private void deductIngredients ( Inventory inventory, Recipe recipe ) {

        Map<String, Integer> inventoryMap = inventory.getIngredients();

        for ( Ingredient ingredient : recipe.getIngredients() ) {

            String ingredientName = ingredient.getName();
            Integer requiredAmount = ingredient.getAmount();

            Integer availableAmount = inventoryMap.getOrDefault( ingredientName, 0 );

            // deduct the ingredient amount required from available
            inventoryMap.put( ingredientName, availableAmount - requiredAmount );
        }

        inventory.setIngredients( inventoryMap );
    }
}
