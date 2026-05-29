package edu.ncsu.csc326.wolfcafe.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;

/**
 * Tests the tax rate repository class
 * 
 * @author Monica Jin
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TaxRateRepositoryTest {

	/** the repository to test **/
    @Autowired
    private TaxRateRepository taxRateRepository;

    /** the tax rate **/
    private TaxRate taxRate;

    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
        taxRate = new TaxRate(null, 2.0);
        taxRateRepository.save(taxRate);
    }

    /**
     * Tests getting the tax rate
     */
    @Test
    public void testGetTaxRate() {
        TaxRate retrieved = taxRateRepository.findAll().get(0);

        assertAll("TaxRate",
            () -> assertNotNull(retrieved.getId()),
            () -> assertEquals(2.0, retrieved.getRate())
        );
    }

    /**
     * Tests updating the tax rate
     */
    @Test
    public void testUpdateTaxRate() {
        TaxRate existing = taxRateRepository.findAll().get(0);
        existing.setRate(8.5);
        taxRateRepository.save(existing);

        TaxRate updated = taxRateRepository.findAll().get(0);
        assertEquals(8.5, updated.getRate());
    }
}