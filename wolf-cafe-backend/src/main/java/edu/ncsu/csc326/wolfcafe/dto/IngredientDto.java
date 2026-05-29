package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This defines the dto for the ingredient entity
 * @author Monica Jin
 */

public class IngredientDto {
	/** ingredient id, primary key for database reference */
	private Long id;
	
	/** dynamic ingredient type */
	private String name;
	
	/** quantity of ingredient */
	private Integer amount;
	
	/**
	 * Empty constructor for Hibernate to load objects from database
	 */
	public IngredientDto() {
		
	}

	/**
	 * Constructor for ingredient entity
	 * @param name the name to set
	 * @param amount the amount to set
	 */
	public IngredientDto(String name, Integer amount) {
		this.name = name;
		this.amount = amount;
	}
	
	/**
	 * Constructor for ingredient entity
	 * @param id the id to set
	 * @param name the name to set
	 * @param amount the amount to set
	 */
	public IngredientDto(Long id, String name, Integer amount) {
		this.id = id;
		this.name = name;
		this.amount = amount;
	}

	/**
	 * Gets the id of the ingredient
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of the ingredient
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * gets the name of the ingredient
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the name of the ingredient
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * gets the amount of the ingredient
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * sets the amount of the ingredient
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
}