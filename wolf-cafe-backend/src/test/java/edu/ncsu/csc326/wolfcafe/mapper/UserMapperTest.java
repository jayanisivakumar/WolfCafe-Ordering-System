package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.UserRequestDTO;
import edu.ncsu.csc326.wolfcafe.dto.UserResponseDTO;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import org.junit.jupiter.api.Test;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    @Test
    void testToResponseDTONull() {
        assertNull(UserMapper.toResponseDTO(null));
    }

    @Test
    void testToResponseDTOWithRole() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setName("ADMIN");
        user.setRoles(Collections.singleton(role));
        UserResponseDTO dto = UserMapper.toResponseDTO(user);
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void testToResponseDTONoRole() {
        User user = new User();
        user.setId(2L);
        user.setUsername("norole");
        user.setEmail("norole@example.com");
        user.setRoles(Collections.emptySet());
        UserResponseDTO dto = UserMapper.toResponseDTO(user);
        assertEquals(2L, dto.getId());
        assertEquals("norole", dto.getUsername());
        assertEquals("norole@example.com", dto.getEmail());
        assertNull(dto.getRole());
    }

    @Test
    void testToEntityNull() {
        assertNull(UserMapper.toEntity(null));
    }

    @Test
    void testToEntity() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("entityuser");
        dto.setEmail("entity@example.com");
        dto.setPassword("password");
        dto.setRole("STAFF");
        User user = UserMapper.toEntity(dto);
        assertEquals("entityuser", user.getUsername());
        assertEquals("entity@example.com", user.getEmail());
    }
}
