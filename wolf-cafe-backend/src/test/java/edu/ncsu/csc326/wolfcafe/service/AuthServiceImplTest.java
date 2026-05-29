package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDto registerDto;
    private Role customerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDto = new RegisterDto("John Doe", "johndoe", "john@example.com", "password123");
        
        customerRole = new Role();
        customerRole.setId(1L);
        customerRole.setName("ROLE_CUSTOMER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should register customer successfully")
    void testRegisterSuccess() {
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(customerRole);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        String response = authService.register(registerDto);

        assertEquals("User registered successfully.", response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(true);

        assertThrows(WolfCafeAPIException.class, () -> authService.register(registerDto));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);

        assertThrows(WolfCafeAPIException.class, () -> authService.register(registerDto));
    }

    @Test
    @DisplayName("Should login successfully and return JWT response")
    void testLoginSuccess() {
        LoginDto loginDto = new LoginDto("johndoe", "password123");
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "johndoe",
                "password123",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        user.setRoles(roles);

        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe"))
                .thenReturn(Optional.of(user));

        JwtAuthResponse response = authService.login(loginDto);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getAccessToken());
        assertEquals("ROLE_CUSTOMER", response.getRole());
    }

    @Test
    @DisplayName("Should login and return role when user found")
    void testLoginWithRole() {
        LoginDto loginDto = new LoginDto("admin", "adminpass");
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "adminpass",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("admin.jwt.token");

        User adminUser = new User();
        adminUser.setUsername("admin");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRoles(roles);

        when(userRepository.findByUsernameOrEmail("admin", "admin"))
                .thenReturn(Optional.of(adminUser));

        JwtAuthResponse response = authService.login(loginDto);

        assertEquals("ROLE_ADMIN", response.getRole());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        authService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void testDeleteUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.deleteUserById(userId));
    }

    @Test
    @DisplayName("Should register admin successfully")
    void testRegisterAdminSuccess() {
        RegisterDto adminRegisterDto = new RegisterDto("Admin User", "admin", "admin@example.com", "adminpass");

        when(userRepository.existsByUsername(adminRegisterDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(adminRegisterDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(passwordEncoder.encode(adminRegisterDto.getPassword())).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        String response = authService.registerAdmin(adminRegisterDto);

        assertEquals("Admin user registered successfully.", response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when admin username already exists")
    void testRegisterAdminDuplicateUsername() {
        RegisterDto adminRegisterDto = new RegisterDto("Admin", "admin", "admin@example.com", "pass");

        when(userRepository.existsByUsername(adminRegisterDto.getUsername())).thenReturn(true);

        assertThrows(WolfCafeAPIException.class, () -> authService.registerAdmin(adminRegisterDto));
    }

    @Test
    @DisplayName("Should throw exception when admin email already exists")
    void testRegisterAdminDuplicateEmail() {
        RegisterDto adminRegisterDto = new RegisterDto("Admin", "admin", "admin@example.com", "pass");

        when(userRepository.existsByUsername(adminRegisterDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(adminRegisterDto.getEmail())).thenReturn(true);

        assertThrows(WolfCafeAPIException.class, () -> authService.registerAdmin(adminRegisterDto));
    }
}
