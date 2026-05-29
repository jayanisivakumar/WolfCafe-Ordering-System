package edu.ncsu.csc326.wolfcafe.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import jakarta.transaction.Transactional;

/**
 * This defines the repository for the ingredient entity
 * @author Monica Jin
 */

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class IngredientRepositoryTest {
	
	@Autowired
	private IngredientRepository ingredientRepository;
	
	private Long ingredient1Id;
	private Long ingredient2Id;

	@BeforeEach
	public void setUp() throws Exception {
		ingredientRepository.deleteAll();
		
		Ingredient ingredient1 = new Ingredient("COFFEE", 5);
		Ingredient ingredient2 = new Ingredient("PUMPKIN SPICE", 10);
		
		ingredient1Id = ingredientRepository.save(ingredient1).getId();
		ingredient2Id = ingredientRepository.save(ingredient2).getId();
		
		System.out.println(ingredient1Id + " " + ingredient2Id);
	}

	@Test
	@Transactional
	public void testAddIngredients() {
		Ingredient i1 = ingredientRepository.findById(ingredient1Id).get();
		assertAll("Ingredient contents",
				() -> assertEquals(ingredient1Id, i1.getId()),
				() -> assertEquals("COFFEE", i1.getName()),
				() -> assertEquals(5, i1.getAmount()));
		
		Ingredient i2 = ingredientRepository.findById(ingredient2Id).get();
		assertAll("Ingredient contents",
				() -> assertEquals(ingredient2Id, i2.getId()),
				() -> assertEquals("PUMPKIN SPICE", i2.getName()),
				() -> assertEquals(10, i2.getAmount()));
	}

}