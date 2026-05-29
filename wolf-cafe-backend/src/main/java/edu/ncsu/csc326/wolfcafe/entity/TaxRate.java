package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The tax rate for the system
 * 
 * @author Monica Jin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tax_rates")
public class TaxRate {

    /** Tax rate id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tax rate percentage */
    @Column(nullable = false)
    private double rate;

}
