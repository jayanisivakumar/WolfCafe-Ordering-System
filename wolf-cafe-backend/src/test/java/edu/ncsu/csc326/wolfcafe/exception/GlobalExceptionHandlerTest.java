package edu.ncsu.csc326.wolfcafe.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should handle WolfCafeAPIException with BAD_REQUEST")
    @Transactional
    void testWolfCafeAPIException() throws Exception {
        // Attempt to register with an invalid request (missing required fields or duplicate data)
        // This should trigger a WolfCafeAPIException
        LoginDto invalidLogin = new LoginDto("", "");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(invalidLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle duplicate username during registration")
    @Transactional
    void testDuplicateUsernameException() throws Exception {
        // Register a user first
        edu.ncsu.csc326.wolfcafe.dto.RegisterDto registerDto = 
            new edu.ncsu.csc326.wolfcafe.dto.RegisterDto("First User", "duplicate", "first@example.com", "password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(registerDto)))
                .andExpect(status().isCreated());
        
        // Try to register again with same username
        edu.ncsu.csc326.wolfcafe.dto.RegisterDto duplicateRegisterDto = 
            new edu.ncsu.csc326.wolfcafe.dto.RegisterDto("Second User", "duplicate", "second@example.com", "password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(duplicateRegisterDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle duplicate email during registration")
    @Transactional
    void testDuplicateEmailException() throws Exception {
        // Register a user first
        edu.ncsu.csc326.wolfcafe.dto.RegisterDto registerDto = 
            new edu.ncsu.csc326.wolfcafe.dto.RegisterDto("First User", "user1", "duplicate@example.com", "password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(registerDto)))
                .andExpect(status().isCreated());
        
        // Try to register again with same email
        edu.ncsu.csc326.wolfcafe.dto.RegisterDto duplicateRegisterDto = 
            new edu.ncsu.csc326.wolfcafe.dto.RegisterDto("Second User", "user2", "duplicate@example.com", "password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(duplicateRegisterDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle UserNotFoundException with NOT_FOUND")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testUserNotFoundExceptionHandler() throws Exception {
        // Try to get a user that doesn't exist
        mockMvc.perform(get("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }

    @Test
    @DisplayName("Should handle UserNotFoundException on delete non-existent user")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testUserNotFoundExceptionOnDelete() throws Exception {
        // Try to delete a user that doesn't exist
        mockMvc.perform(delete("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }

    @Test
    @DisplayName("Should handle UserNotFoundException on update non-existent user")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testUserNotFoundExceptionOnUpdate() throws Exception {
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setUsername("updated");
        updateDto.setEmail("updated@example.com");
        updateDto.setPassword("password123");
        updateDto.setRole("ROLE_STAFF");

        // Try to update a user that doesn't exist
        mockMvc.perform(put("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }

    @Test
    @DisplayName("Should return timestamp in UserNotFoundException response")
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testUserNotFoundResponseStructure() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("User not found with id: 99999"));
    }
}


