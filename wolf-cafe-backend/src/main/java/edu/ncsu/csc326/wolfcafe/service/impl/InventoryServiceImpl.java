/**
 * Represents the Inventory Service of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.service.impl;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
 
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
 
/**
 * Implementation of the InventoryService interface (UC3: Manage Items and Inventory).
 */
@Service
public class InventoryServiceImpl implements InventoryService {
 
    /** Logger for recording staff inventory actions */
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
 
    /**
     * Default constructor
     */
    public InventoryServiceImpl() {
    }
 
    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryRepository inventoryRepository;
 
    /**
     * Returns the username of the currently authenticated user.
     * @return current username or "anonymous"
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
 
    /**
     * Creates the inventory.
     * @param inventoryDto inventory to create
     * @return updated inventory after creation
     */
    @Override
    public InventoryDto createInventory(final InventoryDto inventoryDto) {
        final Inventory inventory = InventoryMapper.mapToInventory(inventoryDto);
        final Inventory savedInventory = inventoryRepository.save(inventory);
        return InventoryMapper.mapToInventoryDto(savedInventory);
    }
 
    /**
     * Returns the single inventory. If none exists, creates one with an empty
     * ingredient map.
     * Corresponds to UC3 Steps 13-14: Staff navigates to inventory page, system
     * displays current inventory levels.
     *
     * @return the existing or newly created InventoryDto
     */
    @Override
    public InventoryDto getInventory() {
        final List<Inventory> inventoryList = inventoryRepository.findAll();
        if (inventoryList.isEmpty()) {
            final Inventory inventory = new Inventory();
            inventory.setIngredients(new HashMap<>());
            final Inventory saved = inventoryRepository.save(inventory);
            return InventoryMapper.mapToInventoryDto(saved);
        }
        return InventoryMapper.mapToInventoryDto(inventoryList.get(0));
    }
 
    /**
     * Updates the inventory by adding the provided ingredient amounts to the
     * current inventory values. All amounts must be positive integers.
     * Corresponds to UC3 Steps 15-17: Staff selects an ingredient, enters a quantity,
     * and the system updates the inventory and logs [username, STAFF, UPDATE_INVENTORY].
     *
     * @param inventoryDto contains ingredient amounts to add
     * @return updated InventoryDto after modification
     * @throws ResourceNotFoundException if no inventory exists
     * @throws IllegalArgumentException  if any ingredient amount is not a positive integer
     */
    @Override
    public InventoryDto updateInventory(final InventoryDto inventoryDto) {
        final List<Inventory> inventoryList = inventoryRepository.findAll();
 
        if (inventoryList.isEmpty()) {
            throw new ResourceNotFoundException("Inventory does not exist.");
        }
 
        final Inventory inventory = inventoryList.get(0);
 
        final Map<String, Integer> currentIngredients = inventory.getIngredients();
        final Map<String, Integer> toBeAdded = inventoryDto.getIngredients();
 
        // Validate all quantities before making any changes
        for (final Integer amount : toBeAdded.values()) {
            if (!validateQuantity(amount == null ? 0 : amount)) {
                throw new IllegalArgumentException("Ingredient units must be positive integers.");
            }
        }
 
        // Add the provided amounts to current inventory levels
        for (final Map.Entry<String, Integer> entry : toBeAdded.entrySet()) {
            final String ingredient = entry.getKey();
            final Integer amountToAdd = entry.getValue();
            final Integer currentAmount = currentIngredients.getOrDefault(ingredient, 0);
            currentIngredients.put(ingredient, currentAmount + amountToAdd);
        }
 
        inventory.setIngredients(currentIngredients);
        final Inventory savedInventory = inventoryRepository.save(inventory);
 
        String username = getCurrentUsername();
        logger.info("[{}, STAFF, UPDATE_INVENTORY]", username);
 
        return InventoryMapper.mapToInventoryDto(savedInventory);
    }
 
    /**
     * Returns true if the given quantity is a valid positive integer (qty &gt; 0).
     * Corresponds to the validateQuantity check shown in the UC3 sequence diagram.
     *
     * @param qty quantity to validate
     * @return true if qty &gt; 0, false otherwise
     */
    @Override
    public boolean validateQuantity(final int qty) {
        return qty > 0;
    }
}