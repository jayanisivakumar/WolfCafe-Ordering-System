package edu.ncsu.csc326.wolfcafe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the ItemController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    /** Mocked MVC */
    @Autowired
    private MockMvc mvc;

    /** Item Service */
    @MockitoBean
    private ItemService itemService;

    /** Object mapper */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** API path */
    private static final String API_PATH    = "/api/items";
    /** Encoding */
    private static final String ENCODING    = "utf-8";
    /** Item name */
    private static final String ITEM_NAME   = "Coffee";
    /** Item description */
    private static final String ITEM_DESCRIPTION = "Coffee is life";
    /** Item price */
    private static final double ITEM_PRICE  = 3.25;

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Builds a minimal ItemDto with no ingredients.
     */
    private ItemDto buildItemDto ( String name, String description, double price ) {
        ItemDto dto = new ItemDto();
        dto.setName( name );
        dto.setDescription( description );
        dto.setPrice( price );
        return dto;
    }

    // ── POST /api/items ───────────────────────────────────────────────────────

    /**
     * Tests that a STAFF user can create an item and receives 201 Created.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItem () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESCRIPTION ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /**
     * Tests that a STAFF user can create an item that includes an ingredients list.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItemWithIngredients () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "ESPRESSO", 2 ) );
        itemDto.getIngredients().add( new IngredientDto( "MILK", 1 ) );

        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) );
    }

    /**
     * Tests that an unauthenticated request to create an item returns 401.
     */
    @Test
    public void testCreateItemNotAuthenticated () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() );
    }

    /**
     * Tests that a CUSTOMER cannot create an item (403 Forbidden).
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCreateItemAsCustomerForbidden () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Tests that creating an item with a duplicate name returns 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItemWithDuplicateName () throws Exception {
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new IllegalArgumentException( "Duplicate item name" ) );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE ) ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests that creating an item with a negative price returns 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItemWithNegativePrice () throws Exception {
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new IllegalArgumentException( "Price cannot be negative" ) );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, -10.0 ) ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests that creating an item with a blank name returns 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItemWithBlankName () throws Exception {
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new IllegalArgumentException( "Item name cannot be blank" ) );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( buildItemDto( "", ITEM_DESCRIPTION, ITEM_PRICE ) ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests that creating an item with an ingredient not in inventory returns 400.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testCreateItemIngredientNotInInventory () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.getIngredients().add( new IngredientDto( "VANILLA", 2 ) );

        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new IllegalArgumentException(
                        "Ingredient 'VANILLA' does not exist in the inventory." ) );

        mvc.perform( post( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    // ── GET /api/items ────────────────────────────────────────────────────────

    /**
     * Tests getting a single item by id as STAFF returns 200 with correct fields.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testGetItemById () throws Exception {
        ItemDto itemDto = buildItemDto( ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE );
        itemDto.setId( 27L );
        Mockito.when( itemService.getItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        mvc.perform( get( API_PATH + "/27" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 27 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESCRIPTION ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /**
     * Tests getting all items as STAFF returns 200 with all items.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testGetAllItemsAsStaff () throws Exception {
        ItemDto item1 = buildItemDto( "Coffee", "Coffee is life", 3.25 );
        item1.setId( 1L );
        ItemDto item2 = buildItemDto( "Tea", "Tea time", 2.50 );
        item2.setId( 2L );

        Mockito.when( itemService.getAllItems() ).thenReturn( Arrays.asList( item1, item2 ) );

        mvc.perform( get( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$[0].id", Matchers.equalTo( 1 ) ) )
                .andExpect( jsonPath( "$[0].name", Matchers.equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$[1].id", Matchers.equalTo( 2 ) ) )
                .andExpect( jsonPath( "$[1].name", Matchers.equalTo( "Tea" ) ) );
    }

    /**
     * Tests getting all items as CUSTOMER returns 200.
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testGetAllItemsAsCustomer () throws Exception {
        ItemDto item1 = buildItemDto( "Coffee", "Coffee is life", 3.25 );
        item1.setId( 1L );
        Mockito.when( itemService.getAllItems() ).thenReturn( Arrays.asList( item1 ) );

        mvc.perform( get( API_PATH )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );
    }

    /**
     * Tests getting all items as ADMIN returns 200.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testGetAllItemsAsAdmin () throws Exception {
        ItemDto item1 = buildItemDto( "Coffee", "desc", 3.25 );
        item1.setId( 1L );
        Mockito.when( itemService.getAllItems() ).thenReturn( Arrays.asList( item1 ) );

        mvc.perform( get( API_PATH ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );
    }

    // ── PUT /api/items/{id} ───────────────────────────────────────────────────

    /**
     * Tests that a STAFF user can update an item and receives 200 with updated fields.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateItem () throws Exception {
        ItemDto updatedItemDto = buildItemDto( "Updated Coffee", "Updated description", 4.50 );
        updatedItemDto.setId( 1L );

        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 1L ), ArgumentMatchers.any() ) )
                .thenReturn( updatedItemDto );

        mvc.perform( put( API_PATH + "/1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( updatedItemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 1 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( "Updated Coffee" ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( 4.50 ) ) );
    }

    /**
     * Tests that updating an item with invalid data returns 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateItemWithBadRequest () throws Exception {
        ItemDto itemDto = buildItemDto( "", "", -5.0 );
        itemDto.setId( 1L );

        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 1L ), ArgumentMatchers.any() ) )
                .thenThrow( new IllegalArgumentException( "Invalid item data" ) );

        mvc.perform( put( API_PATH + "/1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( itemDto ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() );
    }

    /**
     * Tests that a CUSTOMER cannot update an item (403 Forbidden).
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testUpdateItemAsCustomer () throws Exception {
        mvc.perform( put( API_PATH + "/1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .characterEncoding( ENCODING )
                        .content( MAPPER.writeValueAsString( buildItemDto( "Coffee", "desc", 3.25 ) ) )
                        .accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }

    // ── DELETE /api/items/{id} ────────────────────────────────────────────────

    /**
     * Tests that a STAFF user can delete an item and receives 200 with a success message.
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testDeleteItem () throws Exception {
        Mockito.doNothing().when( itemService ).deleteItem( ArgumentMatchers.any() );

        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.containsString( "Item deleted successfully" ) ) );
    }

    /**
     * Tests that an ADMIN user can delete an item and receives 200.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testDeleteItemAsAdmin () throws Exception {
        Mockito.doNothing().when( itemService ).deleteItem( ArgumentMatchers.any() );

        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() );
    }

    /**
     * Tests that a CUSTOMER cannot delete an item (403 Forbidden).
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testDeleteItemAsCustomer () throws Exception {
        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
    
    /**
     * Test getting all items as an anonymous (unauthenticated) user.
     * UC5: Guest should be able to browse items without logging in.
     * @throws Exception if error
     */
    @Test
    public void testGetAllItemsAsAnonymous() throws Exception {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Coffee");
        item1.setDescription("Coffee is life");
        item1.setPrice(3.25);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Tea");
        item2.setDescription("Tea time");
        item2.setPrice(2.50);

        List<ItemDto> items = Arrays.asList(item1, item2);
        Mockito.when(itemService.getAllItems()).thenReturn(items);

        mvc.perform(get(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(ENCODING)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", Matchers.equalTo("Coffee")))
                .andExpect(jsonPath("$[1].name", Matchers.equalTo("Tea")));
    }

    /**
     * Test getting all items as anonymous when the list is empty.
     * UC5: Should still return 200 with an empty list, not 401/403.
     * @throws Exception if error
     */
    @Test
    public void testGetAllItemsAsAnonymous_emptyList() throws Exception {
        Mockito.when(itemService.getAllItems()).thenReturn(Arrays.asList());

        mvc.perform(get(API_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    /**
     * Test getting a single item by ID as an anonymous (unauthenticated) user.
     * UC5: Guest should be able to view item details without logging in.
     * @throws Exception if error
     */
    @Test
    public void testGetItemByIdAsAnonymous() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);
        itemDto.setPrice(ITEM_PRICE);

        Mockito.when(itemService.getItem(1L)).thenReturn(itemDto);

        mvc.perform(get(API_PATH + "/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.name", Matchers.equalTo(ITEM_NAME)))
                .andExpect(jsonPath("$.price", Matchers.equalTo(ITEM_PRICE)));
    }

    /**
     * Test that an anonymous user cannot create an item (UC5 write protection).
     * POST /api/items should return 401 Unauthorized without a token.
     * @throws Exception if error
     */
    @Test
    public void testCreateItemAsAnonymous_returns401() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);
        itemDto.setPrice(ITEM_PRICE);

        String json = MAPPER.writeValueAsString(itemDto);

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(ENCODING)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test that an anonymous user cannot update an item.
     * PUT /api/items/{id} should return 401 Unauthorized without a token.
     * @throws Exception if error
     */
    @Test
    public void testUpdateItemAsAnonymous_returns401() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(ITEM_NAME);
        itemDto.setDescription(ITEM_DESCRIPTION);
        itemDto.setPrice(ITEM_PRICE);

        String json = MAPPER.writeValueAsString(itemDto);

        mvc.perform(put(API_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(ENCODING)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test that an anonymous user cannot delete an item.
     * DELETE /api/items/{id} should return 401 Unauthorized without a token.
     * @throws Exception if error
     */
    @Test
    public void testDeleteItemAsAnonymous_returns401() throws Exception {
        mvc.perform(delete(API_PATH + "/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    
}
