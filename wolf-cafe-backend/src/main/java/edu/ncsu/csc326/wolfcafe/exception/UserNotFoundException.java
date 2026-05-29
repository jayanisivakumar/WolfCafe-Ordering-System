package edu.ncsu.csc326.wolfcafe.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException ( final Long id ) {
        super( "User not found with id: " + id );
    }
}
