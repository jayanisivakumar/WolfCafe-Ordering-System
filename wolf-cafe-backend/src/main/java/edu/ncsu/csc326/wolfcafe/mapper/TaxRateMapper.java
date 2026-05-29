package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;

/**
 * Represents the TaxRateMapper of the WolfCafe system.
 *
 * @author Monica Jin
 */

public class TaxRateMapper {

    /**
     * Default constructor
     *
     */
    public TaxRateMapper () {
    }
    
    /**
     * Converts a TaxRate entity to TaxRateDto
     *
     * @param taxRate
     *            TaxRate to convert
     * @return TaxRateDto object
     */
    public static TaxRateDto toDto ( final TaxRate taxRate ) {
        final TaxRateDto taxRateDto = new TaxRateDto();
        taxRateDto.setId( taxRate.getId() );
        taxRateDto.setRate( taxRate.getRate() );
        return taxRateDto;
    }
    
    /**
     * Maps from TaxRateDto to taxRate
     *
     * @param taxRateDto
     *            the TaxRateDto to map
     * @return the TaxRate
     */
    public static TaxRate toEntity ( final TaxRateDto taxRateDto ) {
        final TaxRate taxRate = new TaxRate();
        taxRate.setId( taxRateDto.getId() );
        taxRate.setRate( taxRateDto.getRate() );
        return taxRate;
    }
}
