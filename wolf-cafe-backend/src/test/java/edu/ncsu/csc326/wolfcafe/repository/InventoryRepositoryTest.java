/**
 * Test class of the InventoryRepository of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.repository;
 
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.util.HashMap;
import java.util.Map;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
 
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import jakarta.persistence.EntityManager;
 
/**
 * Tests InventoryRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase ( replace = Replace.NONE )
@Transactional
public class InventoryRepositoryTest {
 
    /**
     * Default constructor
     */
    public InventoryRepositoryTest() {
    }
 
    /** Reference to inventory repository */
    @Autowired
    private InventoryRepository inventoryRepository;
 
    /** Reference to EntityManager */
    @Autowired
    private EntityManager testEntityManager;
 
    /** Reference to inventory */
    private Inventory inventory;
 
    /**
     * Sets up the test case. We assume only one inventory row.
     *
     * @throws Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        testEntityManager.createNativeQuery( "DELETE FROM inventory_ingredients" ).executeUpdate();
        testEntityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();
 
        inventory = new Inventory();
 
        final Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put( "Vanilla", 20 );
        ingredients.put( "OatMilk", 14 );
        ingredients.put( "Matcha", 32 );
        ingredients.put( "PumpkinSpice", 10 );
 
        inventory.setIngredients( ingredients );
        inventoryRepository.save( inventory );
    }
 
    /**
     * Tests saving the inventory and retrieving it from the repository.
     */
    @Test
    public void testSaveAndGetInventory() {
        final Inventory fetched = inventoryRepository.findById( inventory.getId() ).get();
        assertAll( "Inventory contents",
                () -> assertEquals( 20, fetched.getIngredients().get( "Vanilla" ) ),
                () -> assertEquals( 14, fetched.getIngredients().get( "OatMilk" ) ),
                () -> assertEquals( 32, fetched.getIngredients().get( "Matcha" ) ),
                () -> assertEquals( 10, fetched.getIngredients().get( "PumpkinSpice" ) ) );
    }
 
    /**
     * Tests updating ingredient quantities in the inventory.
     */
    @Test
    public void testUpdateInventory() {
        final Inventory fetched = inventoryRepository.findById( inventory.getId() ).get();
        fetched.getIngredients().put( "Vanilla", 13 );
        fetched.getIngredients().put( "OatMilk", 7 );
 
        final Inventory updated = inventoryRepository.save( fetched );
        assertAll( "Updated inventory contents",
                () -> assertEquals( 13, updated.getIngredients().get( "Vanilla" ) ),
                () -> assertEquals( 7, updated.getIngredients().get( "OatMilk" ) ) );
    }
 
    /**
     * Tests Inventory constructor with null ingredients — should default to empty map.
     */
    @Test
    public void testInventoryConstructorWithNull() {
        final Inventory inv = new Inventory( 1L, null );
        assertNotNull( inv );
        assertNotNull( inv.getIngredients() );
        assertTrue( inv.getIngredients().isEmpty() );
    }
 
    /**
     * Tests Inventory constructor with a pre-populated ingredients map.
     */
    @Test
    public void testInventoryConstructorWithIngredients() {
        final Map<String, Integer> map = new HashMap<>();
        map.put( "Vanilla", 10 );
        final Inventory inv = new Inventory( 1L, map );
        assertEquals( 10, inv.getIngredients().get( "Vanilla" ) );
    }
 
    /**
     * Tests InventoryDto constructor with ingredients.
     */
    @Test
    @Transactional
    public void testInventoryDtoConstructor() {
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put( "Vanilla", 5 );
        InventoryDto dto = new InventoryDto( 1L, ingredients );
        assertEquals( 1L, dto.getId() );
        assertEquals( 5, dto.getIngredients().get( "Vanilla" ) );
    }
}
