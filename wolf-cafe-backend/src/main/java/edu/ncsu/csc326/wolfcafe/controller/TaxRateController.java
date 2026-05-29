package edu.ncsu.csc326.wolfcafe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;

/**
 * Controller for Tax Rate
 *
 * @author Monica Jin
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/taxrate")
public class TaxRateController {

    /** Connection to TaxRateService */
    @Autowired
    private TaxRateService taxRateService;

    /**
     * GET current tax rate
     * 
     * @return the tax rate dto
     */
    @GetMapping
    public ResponseEntity<TaxRateDto> getTaxRate() {
        return ResponseEntity.ok(taxRateService.getTaxRate());
    }

    /**
     * Validate tax rate input
     * 
     * @param dto the tax rate dto to validate
     * @return whether or not it is validated
     */
    private String validateTaxRate(final TaxRateDto dto) {
        if (dto == null) {
            return "Tax rate cannot be null";
        }

        if (dto.getRate() <= 0) {
            return "Tax rate must be a positive number";
        }

        return null;
    }

    /**
     * Update tax rate
     * 
     * @param dto the tax rate dto to set
     * @return a response entity (was it valid?)
     */
    @PutMapping
    public ResponseEntity<?> setTaxRate(@RequestBody final TaxRateDto dto) {

        final String error = validateTaxRate(dto);
        if (error != null) {
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        try {
            final TaxRateDto updated = taxRateService.setTaxRate(dto);
            return ResponseEntity.ok(updated);
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}