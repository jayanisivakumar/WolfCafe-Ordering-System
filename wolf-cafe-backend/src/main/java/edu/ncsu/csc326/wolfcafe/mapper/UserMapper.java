package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.entity.User;

public class UserMapper {
    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;
        String role = user.getRoles() != null && !user.getRoles().isEmpty()
            ? user.getRoles().iterator().next().getName() : null;
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            role
        );
    }

    public static User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        // Password and roles should be set in the service layer
        return user;
    }
}
