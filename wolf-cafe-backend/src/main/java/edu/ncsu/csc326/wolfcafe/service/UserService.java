package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;

/**
 * Service interface for user management.
 */
public interface UserService {
    UserResponseDTO createUser ( UserRequestDTO userRequestDTO );

    UserResponseDTO getUserById ( Long id );

    List<UserResponseDTO> getAllUsers ();

    UserResponseDTO updateUser ( Long id, UserRequestDTO userRequestDTO );

    void deleteUser ( Long id );
}
