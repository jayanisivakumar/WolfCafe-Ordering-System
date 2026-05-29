package edu.ncsu.csc326.wolfcafe.dto;

/**
 * DTO for user responses.
 */
public class UserResponseDTO {
    private Long   id;
    private String username;
    private String email;
    private String name;
    private String role;

    public UserResponseDTO () {
    }

    public UserResponseDTO ( final Long id, final String username, final String email, final String name, final String role ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId () {
        return id;
    }

    public void setId ( final Long id ) {
        this.id = id;
    }

    public String getUsername () {
        return username;
    }

    public void setUsername ( final String username ) {
        this.username = username;
    }
    
    public String getName () {
        return name;
    }

    public void setName ( final String name ) {
        this.name = name;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail ( final String email ) {
        this.email = email;
    }

    public String getRole () {
        return role;
    }

    public void setRole ( final String role ) {
        this.role = role;
    }
}
