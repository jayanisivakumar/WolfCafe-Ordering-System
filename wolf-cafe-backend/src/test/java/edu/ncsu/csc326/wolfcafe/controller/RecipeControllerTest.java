/**
 * Test class of the RecipeController of the CoffeeMaker system.
 *
 * @author Jayani Sivakumar
 */
package edu.ncsu.csc326.wolfcafe.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;

/**
 * Tests RecipeController
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecipeControllerTest {

    /**
     * Default constructor
     *
     */
    public RecipeControllerTest () {

    }

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc          mvc;

    /** Reference to recipe repository */
    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Sets up the test case.
     *
     * @throws java.lang.Exception
     *             if error
     */
    @BeforeEach
    public void setUp () throws Exception {
        recipeRepository.deleteAll();
    }

    /**
     * Tests getting all recipes
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testGetRecipes () throws Exception {
        final String recipe = mvc.perform( get( "/api/recipes" ) ).andDo( print() ).andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();
        assertFalse( recipe.contains( "Mocha" ) );
    }

    /**
     * Tests creating a recipe
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testCreateRecipe () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );

        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.name" ).value( "Mocha" ) )
                .andExpect( jsonPath( "$.price" ).value( "200" ) );
    }

    /**
     * Tests getting a recipe
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testGetRecipe () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );

        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto ) ) ).andExpect( status().isOk() );
        mvc.perform( get( "/api/recipes/name/Mocha" ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "Mocha" ) ).andExpect( jsonPath( "$.price" ).value( 200 ) );
    }

    /**
     * Tests duplicate recipes
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    // Duplicate names should throw an error
    public void testCreateDuplicate () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );

        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() );
    }

    /**
     * Tests adding more than 3 recipes
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    // More than 3 recipes should throw an error
    public void testTooManyRecipes () throws Exception {
        final RecipeDto recipeDto1 = new RecipeDto( "Mocha", 200 );
        recipeDto1.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto1.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );
        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto1 ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        final RecipeDto recipeDto2 = new RecipeDto( "Coffee", 200 );
        recipeDto2.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto2 ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        final RecipeDto recipeDto3 = new RecipeDto( "Chocolate", 200 );
        recipeDto3.getIngredients().add( new IngredientDto( "CHOCOLATE", 5 ) );
        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto3 ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        final RecipeDto recipeDto4 = new RecipeDto( "Cake", 200 );
        recipeDto4.getIngredients().add( new IngredientDto( "SUGAR", 5 ) );
        recipeDto4.getIngredients().add( new IngredientDto( "FLOUR", 1 ) );
        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto4 ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isInsufficientStorage() );
    }

    /**
     * Tests deleting a recipe
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    // Test delete
    public void testDeleteRecipe () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );
        mvc.perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );

        final String result = mvc.perform( get( "/api/recipes/name/Mocha" ) ).andExpect( status().isOk() ).andReturn()
                .getResponse().getContentAsString();

        final int id = JsonPath.read( result, "$.id" );

        mvc.perform( delete( "/api/recipes/" + id ) ).andExpect( status().isOk() );

        mvc.perform( get( "/api/recipes" ) ).andExpect( status().isOk() ).andExpect( content().string( "[]" ) );
    }

    /**
     * Tests successfully updating a recipe
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testUpdateRecipeSuccess () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );
        recipeDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 1 ) );

        final String createdJson = mvc
                .perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final Number idNum = com.jayway.jsonpath.JsonPath.read( createdJson, "$.id" );
        final Long id = idNum.longValue();

        final RecipeDto updateDto = new RecipeDto( "Mocha", 250 );
        updateDto.getIngredients().add( new IngredientDto( "COFFEE", 6 ) );
        updateDto.getIngredients().add( new IngredientDto( "CHOCOLATE", 2 ) );

        mvc.perform( put( "/api/recipes/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( updateDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.name" ).value( "Mocha" ) )
                .andExpect( jsonPath( "$.price" ).value( 250 ) )
                .andExpect( jsonPath( "$.ingredients.length()" ).value( 2 ) );
    }

    /**
     * Tests updating a recipe with an invalid price
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testUpdateRecipe_InvalidPrice () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );

        final String createdJson = mvc
                .perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final Number idNum = com.jayway.jsonpath.JsonPath.read( createdJson, "$.id" );
        final Long id = idNum.longValue();

        final RecipeDto badPrice = new RecipeDto( "Mocha", 0 );
        badPrice.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );

        mvc.perform( put( "/api/recipes/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( badPrice ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests updating a recipe with no ingredients
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testUpdateRecipe_NoIngredients () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );

        final String createdJson = mvc
                .perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final Number idNum = com.jayway.jsonpath.JsonPath.read( createdJson, "$.id" );
        final Long id = idNum.longValue();

        final RecipeDto noIngredients = new RecipeDto( "Mocha", 250 ); // no
                                                                       // ingredients
                                                                       // added

        mvc.perform( put( "/api/recipes/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( noIngredients ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests updating a recipe with an invalid unit in one of ingredient
     * quantities
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testUpdateRecipe_InvalidUnit () throws Exception {
        final RecipeDto recipeDto = new RecipeDto( "Mocha", 200 );
        recipeDto.getIngredients().add( new IngredientDto( "COFFEE", 5 ) );

        final String createdJson = mvc
                .perform( post( "/api/recipes" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( recipeDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        final Number idNum = com.jayway.jsonpath.JsonPath.read( createdJson, "$.id" );
        final Long id = idNum.longValue();

        final RecipeDto badUnit = new RecipeDto( "Mocha", 250 );
        badUnit.getIngredients().add( new IngredientDto( "COFFEE", -1 ) );

        mvc.perform( put( "/api/recipes/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( badUnit ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Update a recipe that doesn't exist
     *
     * @throws Exception
     *             if invalid
     */
    @Test
    @Transactional
    public void testUpdateRecipe_CannotEdit_NotFound () throws Exception {
        final RecipeDto updateDto = new RecipeDto( "Ghost", 100 );
        updateDto.getIngredients().add( new IngredientDto( "COFFEE", 1 ) );

        mvc.perform( put( "/api/recipes/999" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( updateDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() );
    }
}
