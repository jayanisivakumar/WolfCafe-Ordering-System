package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;

/**
 * Tests the tax rate service class
 * 
 * @author Monica Jin
 */
@SpringBootTest
@Transactional
public class TaxRateServiceTest {

	/** the tax rate service to test **/
    @Autowired
    private TaxRateService taxRateService;

    /** the tax rate repository **/
    @Autowired
    private TaxRateRepository taxRateRepository;

    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
    }

    /**
     * Tests that the default tax rate is created
     */
    @Test
    public void testDefaultTaxRate() {
        TaxRateDto rate = taxRateService.getTaxRate();

        assertNotNull(rate);
        assertEquals(2.0, rate.getRate());
    }

    /**
     * Tests setting a valid tax rate
     */
    @Test
    public void testSetTaxRateValid() {
        taxRateService.getTaxRate();

        TaxRateDto updated = taxRateService.setTaxRate(new TaxRateDto(null, 10.0));

        assertEquals(10.0, updated.getRate());
    }

    /**
     * Tests setting an invalid tax rate
     */
    @Test
    public void testSetTaxRateInvalid() {
        taxRateService.getTaxRate();

        assertThrows(IllegalArgumentException.class, () -> {
            taxRateService.setTaxRate(new TaxRateDto(null, -5.0));
        });
    }

    /**
     * Tests updating the tax rate
     */
    @Test
    public void testTaxRateUpdate() {
        taxRateService.getTaxRate();

        taxRateService.setTaxRate(new TaxRateDto(null, 8.0));

        TaxRateDto retrieved = taxRateService.getTaxRate();
        assertEquals(8.0, retrieved.getRate());
    }
    
    /**
     * toEntity() test
     */
    @Test
    public void testMapperToEntity() {
        TaxRateDto dto = new TaxRateDto(1L, 6.5);

        TaxRate entity = edu.ncsu.csc326.wolfcafe.mapper.TaxRateMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(6.5, entity.getRate());
    }
}