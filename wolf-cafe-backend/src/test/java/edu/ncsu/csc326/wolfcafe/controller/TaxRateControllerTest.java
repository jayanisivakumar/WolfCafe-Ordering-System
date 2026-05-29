package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;

/**
 * Tests tax rate controller
 * 
 * @author Monica Jin
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class TaxRateControllerTest {

	/** the mvc to test **/
    @Autowired
    private MockMvc mvc;

    /** the tax rate repository **/
    @Autowired
    private TaxRateRepository taxRateRepository;

    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
    }

    /**
     * Tests get mapping
     * @throws Exception if invalid
     */
    @Test
    public void testGetTaxRate() throws Exception {
        mvc.perform(get("/api/taxrate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rate").value(2.0));
    }

    /**
     * Tests put mapping for valid tax rate
     * @throws Exception if invalid
     */
    @Test
    public void testSetTaxRateValid() throws Exception {
        TaxRateDto dto = new TaxRateDto(null, 10.0);

        mvc.perform(put("/api/taxrate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rate").value(10.0));
    }

    /**
     * Tests put mapping for invalid tax rate
     * @throws Exception if invalid
     */
    @Test
    public void testSetTaxRateInvalid() throws Exception {
        TaxRateDto dto = new TaxRateDto(null, -5.0);

        mvc.perform(put("/api/taxrate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
            .andExpect(status().isBadRequest());
    }
}