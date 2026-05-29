package edu.ncsu.csc326.wolfcafe.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(42L);
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setRole("ADMIN");
        assertEquals(42L, dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        UserResponseDTO dto = new UserResponseDTO(7L, "bob", "bob@example.com","bob", "STAFF");
        assertEquals(7L, dto.getId());
        assertEquals("bob", dto.getUsername());
        assertEquals("bob@example.com", dto.getEmail());
        assertEquals("STAFF", dto.getRole());
    }
}
