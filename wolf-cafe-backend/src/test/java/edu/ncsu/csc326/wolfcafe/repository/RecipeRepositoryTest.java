/**
 * Test class of the RecipeRepository of the CoffeeMaker system.
 * 
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import java.util.Optional;

/**
 * Tests Recipe repository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RecipeRepositoryTest {
	
	/** Reference to recipe repository */
	@Autowired
	private RecipeRepository recipeRepository;
	
	/** Coffee recipe */
	private Recipe recipe1;
	/** Latte recipe */
	private Recipe recipe2;

	/**
	 * Sets up the test case.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		recipeRepository.deleteAll();
		
		recipe1 = new Recipe("COFFEE", 50);
		recipe2 = new Recipe("LATTE", 100);

		recipeRepository.save(recipe1);
		recipeRepository.save(recipe2);
	}

	/**
	 * Tests retrieving a recipe by name when the recipe exists.
	 */
	@Test
    public void testGetRecipeByNameCoffee() {
        Optional<Recipe> recipe = recipeRepository.findByName("COFFEE");
        Recipe actual = recipe.get();

        assertAll("Coffee recipe",
            () -> assertEquals("COFFEE", actual.getName()),
            () -> assertEquals(50, actual.getPrice())
        );
    }
    
	/**
	 * Tests retrieving another existing recipe by name.
	 */
    @Test
    public void testGetRecipeByNameLatte() {
        Optional<Recipe> recipe = recipeRepository.findByName("LATTE");
        Recipe actual = recipe.get();

        assertAll("Latte recipe",
            () -> assertEquals("LATTE", actual.getName()),
            () -> assertEquals(100, actual.getPrice())
        );
    }
    
    /**
     * Tests retrieving a recipe by a name that does not exist.
     */
    @Test
    public void testGetRecipeByNameInvalid() {
        Optional<Recipe> recipe = recipeRepository.findByName("Unknown");
        assertTrue(recipe.isEmpty());
    }
    
    /**
     * Tests the basic getter and setter methods of the Recipe entity.
     */
    @Test
    public void testRecipeSettersAndGetters() {
        Recipe r = new Recipe();

        r.setName("MOCHA");
        r.setPrice(200);

        assertEquals("MOCHA", r.getName());
        assertEquals(200, r.getPrice());
    }
    
    /**
     * Tests  adding ingredients to a recipe
     */
    @Test
	public void testAddIngredients() {
		Recipe recipe1 = new Recipe("COFFEE", 500);
		recipe1.addIngredient(new Ingredient("COFFEE", 3));
		recipe1.addIngredient(new Ingredient("PUMPKIN SPICE", 2));
		recipe1.addIngredient(new Ingredient("MILK", 1));
		
		Recipe savedRecipe = recipeRepository.save(recipe1);
		Optional<Recipe> retrievedRecipe = recipeRepository.findById(savedRecipe.getId());
		assertAll("Recipe contents",
				() -> assertEquals("COFFEE", retrievedRecipe.get().getName()),
				() -> assertEquals(500, retrievedRecipe.get().getPrice()),
				() -> assertEquals(3, retrievedRecipe.get().getIngredients().size()));
		
		Ingredient i1 = retrievedRecipe.get().getIngredients().get(0);
		Ingredient i2 = retrievedRecipe.get().getIngredients().get(1);
		Ingredient i3 = retrievedRecipe.get().getIngredients().get(2);
		
		assertAll("Ingredient contents",
				() -> assertEquals("COFFEE", i1.getName()),
				() -> assertEquals(3, i1.getAmount()));
		
		assertAll("Ingredient contents",
				() -> assertEquals("PUMPKIN SPICE", i2.getName()),
				() -> assertEquals(2, i2.getAmount()));
		
		assertAll("Ingredient contents",
				() -> assertEquals("MILK", i3.getName()),
				() -> assertEquals(1, i3.getAmount()));
	}
}