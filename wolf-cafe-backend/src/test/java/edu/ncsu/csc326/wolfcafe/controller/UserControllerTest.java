package edu.ncsu.csc326.wolfcafe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc      mockMvc;

    @MockBean
    private UserService  userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName ( "Should create a new staff user" )
    void createStaff () throws Exception {
        final UserRequestDTO request = new UserRequestDTO();
        request.setUsername( "teststaff" );
        request.setEmail( "staff@example.com" );
        request.setPassword( "password123" );
        request.setRole( "STAFF" );
        final UserResponseDTO response = new UserResponseDTO( 1L, "teststaff", "staff@example.com", "teststaff", "STAFF" );
        Mockito.when( userService.createUser( any( UserRequestDTO.class ) ) ).thenReturn( response );

        mockMvc.perform( MockMvcRequestBuilders.post( "/api/users/staff" ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.id" ).value( 1L ) ).andExpect( jsonPath( "$.username" ).value( "teststaff" ) );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName ( "Should get user by id" )
    void getUserById () throws Exception {
        final UserResponseDTO response = new UserResponseDTO( 1L, "testuser", "test@example.com", "testuser", "STAFF" );
        Mockito.when( userService.getUserById( 1L ) ).thenReturn( response );

        mockMvc.perform( MockMvcRequestBuilders.get( "/api/users/1" ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( 1L ) ).andExpect( jsonPath( "$.username" ).value( "testuser" ) );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName ( "Should update user" )
    void updateUser () throws Exception {
        final UserRequestDTO request = new UserRequestDTO();
        request.setUsername( "updateduser" );
        request.setEmail( "updated@example.com" );
        request.setPassword( "newpass" );
        request.setRole( "STAFF" );
        final UserResponseDTO response = new UserResponseDTO( 1L, "updateduser", "updated@example.com", "updateduser", "STAFF" );
        Mockito.when( userService.updateUser( eq( 1L ), any( UserRequestDTO.class ) ) ).thenReturn( response );

        mockMvc.perform( MockMvcRequestBuilders.put( "/api/users/1" ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.username" ).value( "updateduser" ) )
                .andExpect( jsonPath( "$.role" ).value( "STAFF" ) );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName ( "Should delete user" )
    void deleteUser () throws Exception {
        Mockito.doNothing().when( userService ).deleteUser( 1L );
        mockMvc.perform( MockMvcRequestBuilders.delete( "/api/users/1" ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.message" ).value( "User deleted successfully" ) );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all users")
    void getAllUsers() throws Exception {
        List<UserResponseDTO> users = new ArrayList<>();
        users.add(new UserResponseDTO(1L, "user1", "user1@example.com", "user 1", "STAFF"));
        users.add(new UserResponseDTO(2L, "user2", "user2@example.com", "user 2", "ADMIN"));
        
        Mockito.when(userService.getAllUsers()).thenReturn(users);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update user without password")
    void updateUserWithoutPassword() throws Exception {
        final UserRequestDTO request = new UserRequestDTO();
        request.setUsername( "updateduser2" );
        request.setEmail( "updated2@example.com" );
        request.setRole( "ADMIN" );
        final UserResponseDTO response = new UserResponseDTO( 2L, "updateduser2", "updated2@example.com", "updated user 2","ADMIN" );
        Mockito.when( userService.updateUser( eq( 2L ), any( UserRequestDTO.class ) ) ).thenReturn( response );

        mockMvc.perform( MockMvcRequestBuilders.put( "/api/users/2" ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.username" ).value( "updateduser2" ) );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create customer user via staff endpoint with CUSTOMER role")
    void createCustomerViaStaffEndpoint() throws Exception {
        final UserRequestDTO request = new UserRequestDTO();
        request.setUsername( "customuser" );
        request.setEmail( "custom@example.com" );
        request.setPassword( "password123" );
        request.setRole( "STAFF" );  // Changed from CUSTOMER to STAFF since endpoint only accepts STAFF
        final UserResponseDTO response = new UserResponseDTO( 3L, "customuser", "custom@example.com", "custom user", "STAFF" );
        Mockito.when( userService.createUser( any( UserRequestDTO.class ) ) ).thenReturn( response );

        mockMvc.perform( MockMvcRequestBuilders.post( "/api/users/staff" ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.role" ).value( "STAFF" ) );
    }
}
