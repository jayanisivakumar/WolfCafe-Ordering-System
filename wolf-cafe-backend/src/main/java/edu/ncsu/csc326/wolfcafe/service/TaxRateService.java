package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;

import java.util.List;

/**
 * Tax Rate service
 * 
 * @author Monica Jin
 */
public interface TaxRateService {

	/**
	 * Gets the tax rate
	 * @return tax rate dto
	 */
    TaxRateDto getTaxRate();

    /**
     * Sets the tax rate
     * @param dto the tax rate dto
     * @return the tax rate dto
     */
    TaxRateDto setTaxRate(TaxRateDto dto);

    /**
     * Makes sure the rate is valid
     * @param rate the rate to check
     * @return true if the rate is valid
     */
    boolean validateRate(double rate);
}
