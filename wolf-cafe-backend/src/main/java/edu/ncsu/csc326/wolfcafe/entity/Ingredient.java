package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This defines the ingredient entity
 *
 * @author Monica Jin
 */

@Entity
@Table ( name = "ingredient" )
public class Ingredient {
    /** ingredient id, primary key for database reference */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long    id;

    /** dynamic ingredient type */
    private String  name;

    /** quantity of ingredient */
    private Integer amount;

    /**
     * Empty constructor for Hibernate to load objects from database
     */
    public Ingredient () {

    }

    /**
     * Constructor for ingredient entity
     *
     * @param name
     *            the name to set
     * @param amount
     *            the amount to set
     */
    public Ingredient ( final String name, final Integer amount ) {
        this.name = name;
        this.amount = amount;
    }

    /**
     * Constructor for ingredient entity
     *
     * @param id
     *            the id to set
     * @param name
     *            the name to set
     * @param amount
     *            the amount to set
     */
    public Ingredient ( final Long id, final String name, final Integer amount ) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    /**
     * Gets the id of the ingredient
     *
     * @return the id
     */
    public Long getId () {
        return id;
    }

    /**
     * Sets the id of the ingredient
     *
     * @param id
     *            the id to set
     */
    public void setId ( final Long id ) {
        this.id = id;
    }

    /**
     * gets the name of the ingredient
     *
     * @return the name
     */
    public String getName () {
        return name;
    }

    /**
     * sets the name of the ingredient
     *
     * @param name
     *            the name to set
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * gets the amount of the ingredient
     *
     * @return the amount
     */
    public Integer getAmount () {
        return amount;
    }

    /**
     * sets the amount of the ingredient
     *
     * @param amount
     *            the amount to set
     */
    public void setAmount ( final Integer amount ) {
        this.amount = amount;
    }

}
