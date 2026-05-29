package edu.ncsu.csc326.wolfcafe.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import edu.ncsu.csc326.wolfcafe.service.AuthService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implemented AuthService
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

	/** User repository */
    private UserRepository userRepository;
    /** Role repository */
    private RoleRepository roleRepository;
    /** Password encoder object */
    private PasswordEncoder passwordEncoder;
    /** Authentication manager */
    private AuthenticationManager authenticationManager;
    /** JWT Token provider for working with user tokens */
    private JwtTokenProvider jwtTokenProvider;

    /**
	 * Registers the given user
	 * @param registerDto new user information
	 * @return message for success or failure
	 */
    @Override
    public String register(RegisterDto registerDto) {
        // Check for duplicates - username
        if(userRepository.existsByUsername(registerDto.getUsername())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }
        // Check for duplicates - email
        if(userRepository.existsByEmail(registerDto.getEmail())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_CUSTOMER");
        roles.add(userRole);

        user.setRoles(roles);

        userRepository.save(user);

        return "User registered successfully.";
    }

    /**
     * Logins in the given user
     * @param loginDto username/email and password
     * @return response with authenticated user
     */
    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(),
                loginDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        Optional<User> userOptional = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());

        String role = null;
        if (userOptional.isPresent()) {
            User loggedInUser = userOptional.get();
            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();

            if (optionalRole.isPresent()) {
                Role userRole = optionalRole.get();
                role = userRole.getName();
            }
        }

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setRole(role);
        jwtAuthResponse.setAccessToken(token);

        return jwtAuthResponse;
    }

    /**
     * Deletes the given user by id
     * @param id id of user to delete
     */
	@Override
	public void deleteUserById(Long id) {
		userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		userRepository.deleteById(id);
	}

	/**
	 * Registers the first admin user (bootstrap)
	 * @param registerDto admin information
	 * @return message for success or failure
	 */
	@Override
	public String registerAdmin(RegisterDto registerDto) {
		// Check for duplicates - username
		if(userRepository.existsByUsername(registerDto.getUsername())) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Username already exists.");
		}
		// Check for duplicates - email
		if(userRepository.existsByEmail(registerDto.getEmail())) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Email already exists.");
		}

		User user = new User();
		user.setName(registerDto.getName());
		user.setUsername(registerDto.getUsername());
		user.setEmail(registerDto.getEmail());
		user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

		Set<Role> roles = new HashSet<>();
		Role adminRole = roleRepository.findByName("ROLE_ADMIN");
		roles.add(adminRole);

		user.setRoles(roles);

		userRepository.save(user);

		return "Admin user registered successfully.";
	}
}
