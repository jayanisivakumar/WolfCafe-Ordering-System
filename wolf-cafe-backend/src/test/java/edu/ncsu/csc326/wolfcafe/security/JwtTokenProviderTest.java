package edu.ncsu.csc326.wolfcafe.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsername() {
        String token = jwtTokenProvider.generateToken(authentication);
        String username = jwtTokenProvider.getUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testValidateToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should fail to validate invalid token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should fail to validate tampered token")
    void testValidateTamperedToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        String tamperedToken = token + "tampered";
        assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should fail on malformed token")
    void testMalformedToken() {
        String malformedToken = "malformed";
        assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken(authentication);
        
        Collection<GrantedAuthority> authorities2 = new ArrayList<>();
        authorities2.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth2 = new UsernamePasswordAuthenticationToken("anotheruser", "password", authorities2);
        String token2 = jwtTokenProvider.generateToken(auth2);
        
        assertNotEquals(token1, token2);
    }
}
