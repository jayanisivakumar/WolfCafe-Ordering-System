package edu.ncsu.csc326.wolfcafe.repository;
 
import edu.ncsu.csc326.wolfcafe.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
 
/**
 * Repository interface for Items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
 
    /**
     * Returns true if an item with the given name already exists.
     * @param name name to check
     * @return true if name exists
     */
    boolean existsByName(String name);
 
    /**
     * Returns true if an item with the given name exists and a different id.
     * Used to detect duplicates on edit.
     * @param name item name
     * @param id   id to exclude
     * @return true if a different item with that name exists
     */
    boolean existsByNameAndIdNot(String name, Long id);
}