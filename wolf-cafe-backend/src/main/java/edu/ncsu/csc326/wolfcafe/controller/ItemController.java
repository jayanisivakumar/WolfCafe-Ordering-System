package edu.ncsu.csc326.wolfcafe.controller;
 
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
/**
 * Controller for API endpoints for an Item.
 */
@RestController
@RequestMapping("api/items")
@AllArgsConstructor
@CrossOrigin("*")
public class ItemController {
 
    /** Link to ItemService */
    private ItemService itemService;
 
    /**
     * Returns all items. 
     * Corresponds to UC3 Steps 1-2: Staff or Admin navigates to item management
     * page and the system displays the list of all existing items.
     *
     * @return list of all items
     */
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<ItemDto> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }
 
    /**
     * Gets an item by id. 
     *
     * @param id item id
     * @return item with the given id
     */
    @GetMapping("{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable("id") Long id) {
        ItemDto item = itemService.getItem(id);
        return ResponseEntity.ok(item);
    }
 
    /**
     * Creates a new item. Accessible to STAFF and ADMIN roles.
     * Corresponds to UC3 Steps 3-6: Staff or Admin creates a new item, system
     * saves it and logs [username, STAFF, CREATE_ITEM].
     *
     * Returns 400 Bad Request if name is blank, price is negative, or name is duplicate.
     * Returns 403 if caller is CUSTOMER.
     *
     * @param itemDto item details to create
     * @return created item with HTTP 201 Created
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody ItemDto itemDto) {
        try {
            ItemDto savedItem = itemService.addItem(itemDto);
            return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

 
    /**
     * Updates an existing item by id. Accessible to STAFF and ADMIN roles.
     * Corresponds to UC3 Steps 7-9: Staff or Admin edits an item, system updates
     * it and logs [username, STAFF, EDIT_ITEM].
     *
     * Returns 400 Bad Request if name is blank, price is negative, or name is duplicate.
     * Returns 403 if caller is CUSTOMER.
     *
     * @param id      id of item to update
     * @param itemDto updated item details
     * @return updated item
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<?> updateItem(@PathVariable("id") Long id, @RequestBody ItemDto itemDto) {
        try {
            ItemDto updatedItem = itemService.updateItem(id, itemDto);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
 
    /**
     * Deletes the item with the given id. Accessible to STAFF and ADMIN roles.
     * Corresponds to UC3 Steps 10-12: Staff or Admin deletes an item, system
     * removes it and logs [username, STAFF, DELETE_ITEM].
     *
     * Returns 403 if caller is CUSTOMER.
     *
     * @param id id of item to delete
     * @return success message
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteItem(@PathVariable("id") Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok("Item deleted successfully");
    }
}