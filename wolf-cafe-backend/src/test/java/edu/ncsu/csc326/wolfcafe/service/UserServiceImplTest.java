package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.exception.UserNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.impl.UserServiceImpl;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

class UserServiceImplTest {
    @Mock
    private UserRepository  userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp () {
        MockitoAnnotations.openMocks( this );
    }

    @Test
    void createUser_success () {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername( "testuser" );
        dto.setEmail( "test@example.com" );
        dto.setPassword( "password" );
        dto.setRole( "ROLE_CUSTOMER" );

        User saved = new User();
        saved.setId( 1L );
        saved.setUsername( dto.getUsername() );
        saved.setEmail( dto.getEmail() );
        saved.setPassword( "encoded" );
        Role role = new Role();
        role.setName( dto.getRole() );
        saved.setRoles( Collections.singleton( role ) );

        when( passwordEncoder.encode( any() ) ).thenReturn( "encoded" );
        when( userRepository.save( any( User.class ) ) ).thenReturn( saved );

        UserResponseDTO result = userService.createUser( dto );
        assertEquals( "testuser", result.getUsername() );
        assertEquals( "test@example.com", result.getEmail() );
        assertEquals( "ROLE_CUSTOMER", result.getRole() );
    }

    @Test
    void getUserById_found () {
        User user = new User();
        user.setId( 1L );
        user.setUsername( "testuser" );
        user.setEmail( "test@example.com" );
        Role role = new Role();
        role.setName( "ROLE_CUSTOMER" );
        user.setRoles( Collections.singleton( role ) );
        when( userRepository.findById( 1L ) ).thenReturn( Optional.of( user ) );
        UserResponseDTO result = userService.getUserById( 1L );
        assertEquals( "testuser", result.getUsername() );
    }

    @Test
    void getUserById_notFound () {
        when( userRepository.findById( 1L ) ).thenReturn( Optional.empty() );
        assertThrows( UserNotFoundException.class, () -> userService.getUserById( 1L ) );
    }

    @Test
    void updateUser_success () {
        User user = new User();
        user.setId( 1L );
        user.setUsername( "olduser" );
        user.setEmail( "old@example.com" );
        user.setPassword( "oldpass" );
        Role oldRole = new Role();
        oldRole.setName( "ROLE_CUSTOMER" );
        user.setRoles( Collections.singleton( oldRole ) );

        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername( "newuser" );
        dto.setEmail( "new@example.com" );
        dto.setPassword( "newpass" );
        dto.setRole( "ROLE_STAFF" );

        when( userRepository.findById( 1L ) ).thenReturn( Optional.of( user ) );
        when( passwordEncoder.encode( "newpass" ) ).thenReturn( "encoded" );
        when( userRepository.save( any( User.class ) ) ).thenReturn( user );

        UserResponseDTO result = userService.updateUser( 1L, dto );
        assertEquals( "newuser", result.getUsername() );
        assertEquals( "new@example.com", result.getEmail() );
        assertEquals( "ROLE_STAFF", result.getRole() );
    }

    @Test
    void updateUser_notFound () {
        UserRequestDTO dto = new UserRequestDTO();
        when( userRepository.findById( 1L ) ).thenReturn( Optional.empty() );
        assertThrows( UserNotFoundException.class, () -> userService.updateUser( 1L, dto ) );
    }

    @Test
    void deleteUser_success () {
        when( userRepository.existsById( 1L ) ).thenReturn( true );
        doNothing().when( userRepository ).deleteById( 1L );
        assertDoesNotThrow( () -> userService.deleteUser( 1L ) );
    }

    @Test
    void deleteUser_notFound () {
        when( userRepository.existsById( 1L ) ).thenReturn( false );
        assertThrows( UserNotFoundException.class, () -> userService.deleteUser( 1L ) );
    }

    @Test
    void getAllUsers_success() {
        java.util.List<User> users = new java.util.ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        Role role = new Role();
        role.setName("ROLE_STAFF");
        user1.setRoles(Collections.singleton(role));
        users.add(user1);

        when(userRepository.findAll()).thenReturn(users);
        
        java.util.List<UserResponseDTO> results = userService.getAllUsers();
        assertEquals(1, results.size());
        assertEquals("user1", results.get(0).getUsername());
    }

    @Test
    void createUser_duplicate_username() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole("ROLE_CUSTOMER");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        assertThrows(edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException.class, 
                () -> userService.createUser(dto));
    }

    @Test
    void createUser_duplicate_email() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole("ROLE_CUSTOMER");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException.class, 
                () -> userService.createUser(dto));
    }

    @Test
    void updateUser_without_password() {
        User user = new User();
        user.setId(1L);
        user.setUsername("olduser");
        user.setEmail("old@example.com");
        user.setPassword("oldpass");
        Role oldRole = new Role();
        oldRole.setName("ROLE_CUSTOMER");
        user.setRoles(Collections.singleton(oldRole));

        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword(null);  // No password change
        dto.setRole("ROLE_STAFF");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.updateUser(1L, dto);
        assertEquals("newuser", result.getUsername());
    }
}
