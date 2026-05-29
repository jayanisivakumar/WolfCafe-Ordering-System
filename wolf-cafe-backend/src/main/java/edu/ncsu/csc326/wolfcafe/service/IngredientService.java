package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;

/**
 * The service layer receives calls from the API endpoints. This
 * is the service function for ingredient
 * 
 * @author Monica Jin
 */
public interface IngredientService {
	
	/**
	 * Creates the ingredient
	 * @param ingredientDto the dto to create
	 * @return the Ingredient dto
	 */
	IngredientDto createIngredient(IngredientDto ingredientDto);
	
	/**
	 * Gets the ingredient using its id
	 * @param ingredientId the id to get
	 * @return the Ingredient Dto
	 */
	IngredientDto getIngredientById(Long ingredientId);
	
	/**
	 * Gets all the ingredients in the list
	 * @return the ingredients
	 */
	List<IngredientDto> getAllIngredients();
	
	/**
	 * Delete an ingredient from the list
	 * @param ingredientId the id to delete
	 */
	void deleteIngredient(Long ingredientId);
	
	/**
	 * Delete the entire ingredient list
	 */
	void deleteAllIngredients();

}