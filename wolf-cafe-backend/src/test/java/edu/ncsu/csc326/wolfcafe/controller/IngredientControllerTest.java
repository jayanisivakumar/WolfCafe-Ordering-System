package edu.ncsu.csc326.wolfcafe.controller;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
 
import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import jakarta.transaction.Transactional;
 
/**
 * Tests for IngredientController REST endpoints.
 *
 * @author Sohini Das
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IngredientControllerTest {
 
    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;
 
    /**
     * Default constructor for IngredientControllerTest.
     */
    public IngredientControllerTest() {
    }
 
    /**
     * Tests successfully creating an ingredient as STAFF via POST.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testCreateIngredient() throws Exception {
        final IngredientDto ingredient1 = new IngredientDto( "COFFEE", 5 );
 
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient1 ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.amount" ).value( "5" ) )
                .andExpect( jsonPath( "$.name" ).value( "COFFEE" ) );
    }
 
    /**
     * Tests that creating a duplicate ingredient returns 400 Bad Request.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testCreateIngredient_Duplicate() throws Exception {
        final IngredientDto ingredient1 = new IngredientDto( "COFFEE", 5 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient1 ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );
 
        final IngredientDto duplicate = new IngredientDto( "COFFEE", 3 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( duplicate ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }
 
    /**
     * Tests that creating an ingredient with an invalid amount returns 400 Bad Request.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testCreateIngredient_InvalidAmount() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", -1 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }
 
    /**
     * Tests that an unauthenticated user cannot create an ingredient.
     * Expects 401 Unauthorized.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    void testCreateIngredient_Unauthenticated() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * Tests that a CUSTOMER cannot create an ingredient.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testCreateIngredient_CustomerForbidden() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * Tests that an ADMIN cannot create an ingredient.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateIngredient_AdminForbidden() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        mvc.perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * Tests successfully retrieving all ingredients as STAFF via GET.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testGetIngredients() throws Exception {
        mvc.perform( get( "/api/ingredients" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );
    }
 
    /**
     * Tests that an unauthenticated user cannot retrieve ingredients.
     * Expects 401 Unauthorized.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    void testGetIngredients_Unauthenticated() throws Exception {
        mvc.perform( get( "/api/ingredients" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * Tests that a CUSTOMER cannot retrieve ingredients.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testGetIngredients_CustomerForbidden() throws Exception {
        mvc.perform( get( "/api/ingredients" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * Tests successfully retrieving an ingredient by id as STAFF via GET.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testGetIngredientById() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        final String response = mvc
                .perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();
 
        final Long id = new com.google.gson.JsonParser()
                .parse( response ).getAsJsonObject().get( "id" ).getAsLong();
 
        mvc.perform( get( "/api/ingredients/" + id ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "COFFEE" ) );
    }
 
    /**
     * Tests that an unauthenticated user cannot retrieve an ingredient by id.
     * Expects 401 Unauthorized.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    void testGetIngredientById_Unauthenticated() throws Exception {
        mvc.perform( get( "/api/ingredients/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * Tests that a CUSTOMER cannot retrieve an ingredient by id.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testGetIngredientById_CustomerForbidden() throws Exception {
        mvc.perform( get( "/api/ingredients/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * Tests successfully deleting an ingredient by id as STAFF via DELETE.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testDeleteIngredient() throws Exception {
        final IngredientDto ingredient = new IngredientDto( "COFFEE", 5 );
        final String response = mvc
                .perform( post( "/api/ingredients" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( ingredient ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();
 
        final Long id = new com.google.gson.JsonParser()
                .parse( response ).getAsJsonObject().get( "id" ).getAsLong();
 
        mvc.perform( delete( "/api/ingredients/" + id ) )
                .andExpect( status().isOk() );
    }
 
    /**
     * Tests that an unauthenticated user cannot delete an ingredient.
     * Expects 401 Unauthorized.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    void testDeleteIngredient_Unauthenticated() throws Exception {
        mvc.perform( delete( "/api/ingredients/1" ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * Tests that a CUSTOMER cannot delete an ingredient.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testDeleteIngredient_CustomerForbidden() throws Exception {
        mvc.perform( delete( "/api/ingredients/1" ) )
                .andExpect( status().isForbidden() );
    }
 
    /**
     * Tests successfully deleting all ingredients as STAFF via DELETE.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "staff", roles = "STAFF")
    void testDeleteAllIngredients() throws Exception {
        mvc.perform( delete( "/api/ingredients" ) )
                .andExpect( status().isOk() );
    }
 
    /**
     * Tests that an unauthenticated user cannot delete all ingredients.
     * Expects 401 Unauthorized.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    void testDeleteAllIngredients_Unauthenticated() throws Exception {
        mvc.perform( delete( "/api/ingredients" ) )
                .andExpect( status().isUnauthorized() );
    }
 
    /**
     * Tests that a CUSTOMER cannot delete all ingredients.
     * Expects 403 Forbidden.
     *
     * @throws Exception if the request fails
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testDeleteAllIngredients_CustomerForbidden() throws Exception {
        mvc.perform( delete( "/api/ingredients" ) )
                .andExpect( status().isForbidden() );
    }
}
 
