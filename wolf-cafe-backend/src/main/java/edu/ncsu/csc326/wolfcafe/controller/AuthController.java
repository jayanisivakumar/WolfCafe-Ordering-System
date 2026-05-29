package edu.ncsu.csc326.wolfcafe.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.service.AuthService;

/**
 * Controller for authentication functionality.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

	/** Link to AuthService */
    private AuthService authService;

    /**
     * Registers a new customer user with the system.
     * @param registerDto object with registration info
     * @return response indicating success or failure
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Logs in the given user
     * @param loginDto user information for login
     * @return object representing the logged in user
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        JwtAuthResponse jwtAuthResponse = authService.login(loginDto);
        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

    /**
     * Registers the first admin user (bootstrap endpoint, no auth required)
     * @param registerDto admin registration info
     * @return response indicating success or failure
     */
    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterDto registerDto) {
        String response = authService.registerAdmin(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Deletes the given user.  Requires the ADMIN role.
     * @param id id of user to delete
     * @return response indicating success or failure
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
    	authService.deleteUserById(id);
    	return ResponseEntity.ok("User deleted successfully.");
    }
    

}
