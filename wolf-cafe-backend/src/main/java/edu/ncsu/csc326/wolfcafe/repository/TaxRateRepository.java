package edu.ncsu.csc326.wolfcafe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;

/**
 * Repository interface for TaxR Rates.
 */
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
	
}
