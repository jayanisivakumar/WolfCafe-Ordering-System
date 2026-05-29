package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;


/**
 * Tests the authorization controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
	
	/** Admin password from application.properties */
	@Value("${app.admin-user-password}")
	private String adminUserPassword;
	
	/** Mocked MVC for testing */
	@Autowired
	private MockMvc mvc;
	
	/**
	 * Tests logging in as an admin user.
	 * @throws Exception if error
	 */
	@Test
	@DisplayName("Should login successfully as admin")
	@Transactional
	public void testLoginAdmin() throws Exception {
		LoginDto loginDto = new LoginDto("admin", adminUserPassword);
		
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(loginDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
	}
	
	/**
	 * Tests creating a customer user and logging in.
	 * @throws Exception if error
	 */
	@Test
	@DisplayName("Should register customer and login successfully")
	@Transactional
	public void testCreateCustomerAndLogin() throws Exception {
		RegisterDto registerDto = new RegisterDto("Jordan Estes", "jestes", "vitae.erat@yahoo.edu", "JXB16TBD4LC");
		
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(content().string("User registered successfully."));
		
		
		LoginDto loginDto = new LoginDto("jestes", "JXB16TBD4LC");
		
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(loginDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));		
	}

	@Test
	@DisplayName("Should not allow duplicate username registration")
	@Transactional
	public void testRegisterDuplicateUsername() throws Exception {
		RegisterDto registerDto1 = new RegisterDto("First User", "duplicateuser", "first@example.com", "password123");
		RegisterDto registerDto2 = new RegisterDto("Second User", "duplicateuser", "second@example.com", "password456");
		
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto1))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
		
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto2))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should not allow duplicate email registration")
	@Transactional
	public void testRegisterDuplicateEmail() throws Exception {
		RegisterDto registerDto1 = new RegisterDto("First User", "user1", "duplicate@example.com", "password123");
		RegisterDto registerDto2 = new RegisterDto("Second User", "user2", "duplicate@example.com", "password456");
		
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto1))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
		
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto2))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should fail login with invalid credentials")
	@Transactional
	public void testLoginInvalidCredentials() throws Exception {
		LoginDto loginDto = new LoginDto("admin", "wrongpassword");
		
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(loginDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should register admin successfully")
	@Transactional
	public void testRegisterAdmin() throws Exception {
		RegisterDto registerDto = new RegisterDto("New Admin", "newadmin", "newadmin@example.com", "adminpass123");
		
		mvc.perform(post("/api/auth/register-admin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(content().string("Admin user registered successfully."));
	}

	@Test
	@DisplayName("Should login as newly registered admin")
	@Transactional
	public void testRegisterAdminAndLogin() throws Exception {
		RegisterDto adminRegisterDto = new RegisterDto("Test Admin", "testadmin", "testadmin@example.com", "testadminpass");
		
		mvc.perform(post("/api/auth/register-admin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(adminRegisterDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
		
		LoginDto loginDto = new LoginDto("testadmin", "testadminpass");
		
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(loginDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
	}
}
