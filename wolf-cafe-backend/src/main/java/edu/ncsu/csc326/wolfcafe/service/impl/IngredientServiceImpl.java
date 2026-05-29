package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.IngredientMapper;
import edu.ncsu.csc326.wolfcafe.repository.IngredientRepository;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;

/**
 * Represents the IngredientService of the CoffeeMaker system.
 *
 * @author Monica Jin
 */

@Service
public class IngredientServiceImpl implements IngredientService {

    /**
     * Connection to the ingredient repository to work with the DAO + database
     */
    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * Repository for item lookups — used to guard against deleting ingredients
     * that are still referenced by existing items.
     */
    @Autowired
    private ItemRepository itemRepository;

    /**
     * Repository for inventory — used to remove the ingredient entry from
     * inventory when the ingredient itself is deleted.
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Service layer for inventory operations (UC7: add ingredient to inventory)
     */
    @Autowired
    private InventoryService     inventoryService;

    /**
     * Default constructor for IngredientServiceImpl.
     */
    public IngredientServiceImpl () {
    }

    /**
     * Creates an ingredient with the given information.
     *
     * @param ingredientDto
     *            ingredient to create
     * @return created ingredient
     */
    @Override
    public IngredientDto createIngredient ( final IngredientDto ingredientDto ) {
        // can't be empty
        if ( ingredientDto.getName() == null || ingredientDto.getName().trim().isEmpty() ) {
            throw new IllegalArgumentException( "Ingredient name cannot be empty" );
        }
        // amount can't be less or equal to 0
        if ( ingredientDto.getAmount() == null || ingredientDto.getAmount() <= 0 ) {
            throw new IllegalArgumentException( "Ingredient amount must be > 0" );
        }

        // set the name
        ingredientDto.setName( ingredientDto.getName().trim().toUpperCase() );

        // cannot be a duplicate
        if ( ingredientRepository.findByName( ingredientDto.getName().trim().toUpperCase() ).isPresent() ) {
            throw new IllegalArgumentException( "Ingredient with name " + ingredientDto.getName() + " already exists" );
        }

        final Ingredient ingredient = IngredientMapper.mapToIngredient( ingredientDto );
        final Ingredient saved = ingredientRepository.save( ingredient );

        // UC7: Add the new ingredient with its initial amount to inventory
        inventoryService.getInventory(); // ensure inventory exists (creates
                                         // empty if needed)
        final Map<String, Integer> toAdd = new HashMap<>();
        toAdd.put( saved.getName(), saved.getAmount() );
        final InventoryDto inventoryUpdate = new InventoryDto();
        inventoryUpdate.setIngredients( toAdd );
        inventoryService.updateInventory( inventoryUpdate );

        return IngredientMapper.mapToIngredientDto( saved );
    }

    /**
     * Returns the ingredient with the given id.
     *
     * @param ingredientId
     *            ingredient's id
     * @return the ingredient with the given id
     * @throws ResourceNotFoundException
     *             if the ingredient doesn't exist
     */
    @Override
    public IngredientDto getIngredientById ( final Long ingredientId ) {
        final Ingredient ingredient = ingredientRepository.findById( ingredientId ).orElseThrow(
                () -> new ResourceNotFoundException( "Ingredient does not exist with id " + ingredientId ) );
        return IngredientMapper.mapToIngredientDto( ingredient );
    }

    /**
     * Returns all ingredients in the system.
     *
     * @return list of all ingredients
     */
    @Override
    public List<IngredientDto> getAllIngredients () {
        return ingredientRepository.findAll().stream().map( IngredientMapper::mapToIngredientDto ).toList();
    }

    /**
     * Deletes the ingredient with the given id.
     * Throws if any existing item depends on this ingredient, to prevent items
     * from referencing an ingredient that no longer exists in inventory.
     * Also removes the ingredient's entry from the inventory map.
     *
     * @param ingredientId
     *            the id of the ingredient to delete
     * @throws ResourceNotFoundException if the ingredient does not exist
     * @throws IllegalArgumentException  if one or more items depend on this ingredient
     */
    @Override
    public void deleteIngredient ( final Long ingredientId ) {
        final Ingredient ingredient = ingredientRepository.findById( ingredientId )
                .orElseThrow( () -> new ResourceNotFoundException(
                        "Ingredient does not exist with id " + ingredientId ) );

        final String ingredientName = ingredient.getName();

        // Guard: refuse deletion if any item uses this ingredient
        boolean usedByItem = itemRepository.findAll().stream()
                .anyMatch( item -> item.getIngredients().stream()
                        .anyMatch( ing -> ingredientName.equalsIgnoreCase( ing.getName() ) ) );
        if ( usedByItem ) {
            throw new IllegalArgumentException(
                    "Cannot delete ingredient '" + ingredientName
                    + "' because it is used by one or more items. "
                    + "Remove or update those items first." );
        }

        // Remove from inventory map so stale entries don't mislead staff
        final List<Inventory> inventories = inventoryRepository.findAll();
        if ( !inventories.isEmpty() ) {
            final Inventory inventory = inventories.get( 0 );
            inventory.getIngredients().remove( ingredientName );
            inventoryRepository.save( inventory );
        }

        ingredientRepository.deleteById( ingredientId );
    }

    /**
     * Deletes all ingredients in the system.
     */
    @Override
    public void deleteAllIngredients () {
        ingredientRepository.deleteAll();
    }
}
