package edu.ncsu.csc326.wolfcafe.entity;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Represents an item for sale in the WolfCafe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
 
	/** Item id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Item name */
    @Column(nullable = false, unique = true)
    private String name;
    
    /** Item description */
    private String description;
    
    /** Item price */
    @Column(nullable = false)
    private double price;
    
    /** List of ingredients required to make this item */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ingredient> ingredients = new ArrayList<>();
    
    /**
     * Adds an ingredient to this item.
     * @param ingredient the ingredient to add
     */
    public void addIngredient(Ingredient ingredient) {
        if (ingredient != null) {
            this.ingredients.add(ingredient);
        }
    }
 
}