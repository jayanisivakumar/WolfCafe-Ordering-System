
package edu.ncsu.csc326.wolfcafe.service.impl;

import edu.ncsu.csc326.wolfcafe.mapper.UserMapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.UserNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserService;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import org.springframework.http.HttpStatus;

/**
 * Implementation of UserService for user management.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl ( final UserRepository userRepository, final PasswordEncoder passwordEncoder ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO createUser ( final UserRequestDTO dto ) {
        // Uniqueness checks
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        final User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // For simplicity, assign a single role by name
        final Role role = new Role();
        role.setName(dto.getRole());
        user.setRoles(Collections.singleton(role));
        final User saved = userRepository.save(user);
    return UserMapper.toResponseDTO(saved);
    }

    @Override
    public UserResponseDTO getUserById ( final Long id ) {
        final User user = userRepository.findById( id ).orElseThrow( () -> new UserNotFoundException( id ) );
    return UserMapper.toResponseDTO( user );
    }

    @Override
    public List<UserResponseDTO> getAllUsers () {
    return userRepository.findAll().stream().map(UserMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUser ( final Long id, final UserRequestDTO dto ) {
        final User user = userRepository.findById( id ).orElseThrow( () -> new UserNotFoundException( id ) );
        user.setName(dto.getName());
        user.setUsername( dto.getUsername() );
        user.setEmail( dto.getEmail() );
        if ( dto.getPassword() != null && !dto.getPassword().isEmpty() ) {
            user.setPassword( passwordEncoder.encode( dto.getPassword() ) );
        }
        final Role role = new Role();
        role.setName( dto.getRole() );
        user.setRoles( new HashSet<>(Collections.singletonList( role )) );
        final User updated = userRepository.save( user );
    return UserMapper.toResponseDTO( updated );
    }

    @Override
    public void deleteUser ( final Long id ) {
        if ( !userRepository.existsById( id ) ) {
            throw new UserNotFoundException( id );
        }
        userRepository.deleteById( id );
    }

    // UserMapper handles entity <-> DTO conversion
}
