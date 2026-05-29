package edu.ncsu.csc326.wolfcafe.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.mapper.TaxRateMapper;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;

/**
 * Implementation of the TaxRateService interface.
 *
 * @author Monica Jin
 */
@Service
public class TaxRateServiceImpl implements TaxRateService {

    /** Connection to repository */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /**
     * Gets the current tax rate
     * @return the tax rate dto
     */
    @Override
    public TaxRateDto getTaxRate() {

        if (taxRateRepository.count() == 0) {
            TaxRate defaultRate = new TaxRate(null, 2.0);
            taxRateRepository.save(defaultRate);
        }

        TaxRate taxRate = taxRateRepository.findAll().get(0);
        return TaxRateMapper.toDto(taxRate);
    }
    
    /**
     * Sets a new tax rate
     * @param dto the tax rate dto to set
     * @return the updated tax rate dto
     */
    @Override
    public TaxRateDto setTaxRate(TaxRateDto dto) {

        if (!validateRate(dto.getRate())) {
            throw new IllegalArgumentException("Tax rate must be a positive number");
        }
        
        // Ensure tax rate exists
        if (taxRateRepository.count() == 0) {
            taxRateRepository.save(new TaxRate(null, 2.0));
        }

        TaxRate taxRate = taxRateRepository.findAll().get(0);

        // Update value
        taxRate.setRate(dto.getRate());

        // Save entity
        TaxRate saved = taxRateRepository.save(taxRate);

        return TaxRateMapper.toDto(saved);
    }

    /**
     * Validates tax rate
     * @param dto the rate to validate
     * @return true if the rate is valid
     */
    @Override
    public boolean validateRate(double rate) {
        return rate > 0;
    }
}