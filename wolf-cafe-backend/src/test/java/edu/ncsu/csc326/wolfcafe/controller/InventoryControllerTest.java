/**
 * Test class of the InventoryController of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.controller;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
 
import jakarta.persistence.EntityManager;
 
/**
 * Tests InventoryController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InventoryControllerTest {
 
    /**
     * Default constructor
     */
    public InventoryControllerTest() {
    }
 
    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;
 
    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;
 
    /**
     * Clears the inventory table before each test.
     *
     * @throws Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        entityManager.createNativeQuery( "DELETE FROM inventory_ingredients" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();
    }
 
    /**
     * UC3 Steps 13-14: STAFF retrieves current inventory — expects 200 OK with
     * an ingredients map in the response body.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testGetInventoryAsStaff() throws Exception {
        mvc.perform( get( "/api/inventory" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.ingredients" ).exists() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — unauthenticated user cannot view inventory.
     * Expects 401 Unauthorized.
     */
    @Test
    public void testGetInventoryUnauthenticated() throws Exception {
        mvc.perform( get( "/api/inventory" ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — CUSTOMER cannot view inventory.
     * Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testGetInventoryAsCustomerForbidden() throws Exception {
        mvc.perform( get( "/api/inventory" ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — ADMIN cannot view inventory per UC3.
     * Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testGetInventoryAsAdminForbidden() throws Exception {
        mvc.perform( get( "/api/inventory" ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * UC3 Steps 15-17: STAFF updates inventory with valid positive quantities —
     * expects 200 OK with updated ingredient values.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateInventoryAsStaff() throws Exception {
        // Ensure inventory exists first
        mvc.perform( get( "/api/inventory" )
                        .with( org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user( "staff" ).roles( "STAFF" ) ) )
                .andExpect( status().isOk() );
 
        final String updateJson = """
                {
                  "ingredients": {
                    "Vanilla": 5,
                    "PumpkinSpice": 8,
                    "Matcha": 3,
                    "OatMilk": 12
                  }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( updateJson )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.ingredients.Vanilla" ).value( 5 ) )
                .andExpect( jsonPath( "$.ingredients.PumpkinSpice" ).value( 8 ) )
                .andExpect( jsonPath( "$.ingredients.Matcha" ).value( 3 ) )
                .andExpect( jsonPath( "$.ingredients.OatMilk" ).value( 12 ) );
    }
 
    /**
     * UC3 Alt: [Invalid Inventory Quantity] — negative quantity returns 400 Bad Request.
     * Validation occurs at the controller layer before the service is called.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateInventoryNegativeQuantityReturnsBadRequest() throws Exception {
        // Ensure inventory exists
        mvc.perform( get( "/api/inventory" ) ).andExpect( status().isOk() );
 
        final String invalidJson = """
                {
                  "ingredients": {
                    "Vanilla": -5
                  }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( invalidJson )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }
 
    /**
     * UC3 Alt: [Invalid Inventory Quantity] — zero quantity returns 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateInventoryZeroQuantityReturnsBadRequest() throws Exception {
        mvc.perform( get( "/api/inventory" ) ).andExpect( status().isOk() );
 
        final String invalidJson = """
                {
                  "ingredients": {
                    "OatMilk": 0
                  }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( invalidJson )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — unauthenticated user cannot update inventory.
     * Expects 401 Unauthorized.
     */
    @Test
    public void testUpdateInventoryUnauthenticated() throws Exception {
        final String updateJson = """
                {
                  "ingredients": { "Vanilla": 5 }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( updateJson ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — CUSTOMER cannot update inventory.
     * Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testUpdateInventoryAsCustomerForbidden() throws Exception {
        final String updateJson = """
                {
                  "ingredients": { "Vanilla": 5 }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( updateJson ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * UC3 Alt: [Unauthorized Access] — ADMIN cannot update inventory per UC3.
     * Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateInventoryAsAdminForbidden() throws Exception {
        final String updateJson = """
                {
                  "ingredients": { "Vanilla": 5 }
                }
                """;
 
        mvc.perform( put( "/api/inventory" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( updateJson ) )
                .andExpect( status().isForbidden() );
    }
}
