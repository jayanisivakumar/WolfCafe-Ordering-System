package edu.ncsu.csc326.wolfcafe.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserService;

/**
 * REST controller for user management (Admin only).
 */
@CrossOrigin("*")
@RestController
@RequestMapping ( "/api/users" )
@Validated
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController ( UserService userService, UserRepository userRepository ) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Get all users.
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<List<UserResponseDTO>> getAllUsers () {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get user by ID.
     * GET /api/users/{id}
     */
    @GetMapping ( "/{id}" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<UserResponseDTO> getUserById ( @PathVariable Long id ) {
        return ResponseEntity.ok(userService.getUserById( id ));
    }

    /**
     * Create a new Staff user.
     * POST /api/users/staff
     */
    @PostMapping ( "/staff" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<UserResponseDTO> createStaff ( @RequestBody UserRequestDTO userRequestDTO ) {
        userRequestDTO.setRole("ROLE_STAFF");

        UserResponseDTO created = userService.createUser( userRequestDTO );
        logAction(getCurrentUsername(), "ADMIN", "CREATE_STAFF");
        return new ResponseEntity<>( created, HttpStatus.CREATED );
    }

    /**
     * Update a user.
     * PUT /api/users/{id}
     */
    @PutMapping ( "/{id}" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<UserResponseDTO> updateUser ( @PathVariable Long id, @RequestBody UserRequestDTO userRequestDTO ) {
        UserResponseDTO updated = userService.updateUser( id, userRequestDTO );
        String logAction = (userRequestDTO.getRole() != null && userRequestDTO.getRole().contains("STAFF")) ? "EDIT_STAFF" : "EDIT_CUSTOMER";
        logAction(getCurrentUsername(), "ADMIN", logAction);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a user.
     * DELETE /api/users/{id}
     */
    @DeleteMapping ( "/{id}" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity<Map<String, Object>> deleteUser ( @PathVariable Long id ) {
        // Prevent self-deletion
        if (isCurrentAdminSameAsId(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Admin cannot delete their own account");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        
        // Determine the user's role before deletion for proper logging
        User userToDelete = userRepository.findById(id).orElse(null);
        String logAction = "DELETE_STAFF"; // Default
        if (userToDelete != null) {
            logAction = (userToDelete.getRoles().stream()
                .anyMatch(r -> r.getName().contains("STAFF"))) ? "DELETE_STAFF" : "DELETE_CUSTOMER";
        }
        
        userService.deleteUser( id );
        logAction(getCurrentUsername(), "ADMIN", logAction);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get current authenticated username.
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "UNKNOWN";
    }

    /**
     * Helper method to check if current admin is trying to delete themselves.
     */
    private boolean isCurrentAdminSameAsId(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
            // Get user from repository by username and compare IDs
            User currentUser = userRepository.findByUsernameOrEmail(userDetails.getUsername(), userDetails.getUsername())
                .orElse(null);
            return currentUser != null && currentUser.getId().equals(userId);
        }
        return false;
    }

    /**
     * Helper method to log actions.
     */
    private void logAction(String username, String role, String action) {
        System.out.println("[" + username + ", " + role + ", " + action + "]");
        // TODO: Replace with actual logging framework (SLF4J, Log4j, etc.)
    }
}
