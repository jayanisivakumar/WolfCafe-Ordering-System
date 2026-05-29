package edu.ncsu.csc326.wolfcafe.service.impl;
 
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
 
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
/**
 * Implemented item service.
 */
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
 
    /** Logger for recording staff actions */
    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);
 
    /** Item repository */
    private ItemRepository itemRepository;

    /** Inventory repository for ingredient validation */
    private InventoryRepository inventoryRepository;
 
    /** Mapper class */
    private ModelMapper modelMapper;
 
    /**
     * Returns the username of the currently authenticated user,
     * or "anonymous" if no authentication context is present.
     * @return current username
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
 
    /**
     * Validates the fields of an ItemDto for create or update operations.
     * @param itemDto the item to validate
     * @throws IllegalArgumentException if name is blank or price is invalid
     */
    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (itemDto.getPrice() < 0) {
            throw new IllegalArgumentException("Item price cannot be negative.");
        }
    }
 
    /**
     * Adds a new item with its required ingredients. Validates input, checks for
     * duplicate names, and ensures all ingredients exist in inventory before creating.
     * Logs [username, STAFF, CREATE_ITEM] on success.
     * @param itemDto item to add (including ingredients list)
     * @return added item
     * @throws IllegalArgumentException if validation fails, name is duplicate,
     *                                  or any ingredient does not exist in inventory
     */
    @Override
    public ItemDto addItem(ItemDto itemDto) {
        validateItem(itemDto);

        if (itemRepository.existsByName(itemDto.getName().trim())) {
            throw new IllegalArgumentException("An item with the name '" + itemDto.getName() + "' already exists.");
        }

        // Validate all requested ingredients exist in inventory
        List<IngredientDto> ingredientDtos = itemDto.getIngredients();
        if (ingredientDtos != null && !ingredientDtos.isEmpty()) {
            List<Inventory> inventories = inventoryRepository.findAll();
            if (inventories.isEmpty()) {
                throw new IllegalArgumentException(
                        "No inventory exists. Please add ingredients to the inventory first.");
            }
            Map<String, Integer> inventoryMap = inventories.get(0).getIngredients();

            for (IngredientDto ing : ingredientDtos) {
                if (ing.getName() == null || ing.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Ingredient name cannot be empty.");
                }
                if (ing.getAmount() == null || ing.getAmount() <= 0) {
                    throw new IllegalArgumentException("Ingredient amount must be greater than 0.");
                }
                String upperName = ing.getName().trim().toUpperCase();
                if (!inventoryMap.containsKey(upperName)) {
                    throw new IllegalArgumentException(
                            "Ingredient '" + ing.getName() + "' does not exist in the inventory. "
                            + "Please add it as an ingredient first.");
                }
            }
        }

        // Build Item entity manually to attach ingredients
        Item item = new Item();
        item.setName(itemDto.getName().trim());
        item.setDescription(itemDto.getDescription());
        item.setPrice(itemDto.getPrice());

        if (ingredientDtos != null) {
            for (IngredientDto ing : ingredientDtos) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ing.getName().trim().toUpperCase());
                ingredient.setAmount(ing.getAmount());
                item.addIngredient(ingredient);
            }
        }

        Item savedItem = itemRepository.save(item);

        String username = getCurrentUsername();
        logger.info("[{}, STAFF, CREATE_ITEM]", username);

        return modelMapper.map(savedItem, ItemDto.class);
    }
 
    /**
     * Gets item by id.
     * @param id id of item to get
     * @return returned item
     */
    @Override
    public ItemDto getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
        return modelMapper.map(item, ItemDto.class);
    }
 
    /**
     * Returns all items.
     * @return all items
     */
    @Override
    public List<ItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(item -> modelMapper.map(item, ItemDto.class))
                .collect(Collectors.toList());
    }
 
    /**
     * Updates an existing item by id. Validates input and checks for duplicate names
     * among other items. Re-validates all ingredients against inventory (same guard
     * as addItem). Logs [username, STAFF, EDIT_ITEM] on success.
     * @param id      id of item to update
     * @param itemDto new values for the item
     * @return updated item
     * @throws IllegalArgumentException if validation fails, name is duplicate,
     *                                  or any ingredient does not exist in inventory
     */
    @Override
    public ItemDto updateItem(Long id, ItemDto itemDto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
 
        validateItem(itemDto);
 
        if (itemRepository.existsByNameAndIdNot(itemDto.getName().trim(), id)) {
            throw new IllegalArgumentException("An item with the name '" + itemDto.getName() + "' already exists.");
        }

        // Validate updated ingredients exist in inventory (mirrors addItem guard)
        List<IngredientDto> ingredientDtos = itemDto.getIngredients();
        if (ingredientDtos != null && !ingredientDtos.isEmpty()) {
            List<Inventory> inventories = inventoryRepository.findAll();
            if (inventories.isEmpty()) {
                throw new IllegalArgumentException(
                        "No inventory exists. Please add ingredients to the inventory first.");
            }
            Map<String, Integer> inventoryMap = inventories.get(0).getIngredients();

            for (IngredientDto ing : ingredientDtos) {
                if (ing.getName() == null || ing.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Ingredient name cannot be empty.");
                }
                if (ing.getAmount() == null || ing.getAmount() <= 0) {
                    throw new IllegalArgumentException("Ingredient amount must be greater than 0.");
                }
                String upperName = ing.getName().trim().toUpperCase();
                if (!inventoryMap.containsKey(upperName)) {
                    throw new IllegalArgumentException(
                            "Ingredient '" + ing.getName() + "' does not exist in the inventory. "
                            + "Please add it as an ingredient first.");
                }
            }
        }

        item.setName(itemDto.getName().trim());
        item.setDescription(itemDto.getDescription());
        item.setPrice(itemDto.getPrice());

        // Replace the ingredient list with the updated one
        item.getIngredients().clear();
        if (ingredientDtos != null) {
            for (IngredientDto ing : ingredientDtos) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ing.getName().trim().toUpperCase());
                ingredient.setAmount(ing.getAmount());
                item.addIngredient(ingredient);
            }
        }

        Item updatedItem = itemRepository.save(item);
 
        String username = getCurrentUsername();
        logger.info("[{}, STAFF, EDIT_ITEM]", username);
 
        return modelMapper.map(updatedItem, ItemDto.class);
    }
 
    /**
     * Deletes the item with the given id.
     * Logs [username, STAFF, DELETE_ITEM] on success.
     * @param id id of item to delete
     */
    @Override
    public void deleteItem(Long id) {
        itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
        itemRepository.deleteById(id);
 
        String username = getCurrentUsername();
        logger.info("[{}, STAFF, DELETE_ITEM]", username);
    }
}