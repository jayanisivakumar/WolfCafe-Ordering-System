package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user creation and update requests.
 */
public class UserRequestDTO {
    @NotBlank ( message = "Name is required" )
    private String name;
	
	@NotBlank ( message = "Username is required" )
    private String username;

    @NotBlank ( message = "Email is required" )
    @Email ( message = "Email should be valid" )
    private String email;

    @NotBlank ( message = "Password is required" )
    @Size ( min = 6, message = "Password must be at least 6 characters" )
    private String password;

    @NotBlank ( message = "Role is required" )
    private String role;    // Should be one of: ROLE_ADMIN, ROLE_STAFF,
                            // ROLE_CUSTOMER

    public UserRequestDTO () {
    }
    
    public String getName () {
        return name;
    }

    public void setName ( final String name ) {
        this.name = name;
    }


    public String getUsername () {
        return username;
    }

    public void setUsername ( final String username ) {
        this.username = username;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail ( final String email ) {
        this.email = email;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword ( final String password ) {
        this.password = password;
    }

    public String getRole () {
        return role;
    }

    public void setRole ( final String role ) {
        this.role = role;
    }
}
