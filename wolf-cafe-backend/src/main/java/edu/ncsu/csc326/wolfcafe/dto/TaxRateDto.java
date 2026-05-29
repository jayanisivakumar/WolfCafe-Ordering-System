package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dto for tax rate
 * 
 * @author Monica Jin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateDto {
	
	/** Tax rate id */
    private Long id;
    
    /** Tax rate percentage */
    private double rate;
}
