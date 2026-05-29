package edu.ncsu.csc326.wolfcafe.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;


/**
 * Sets up the database with roles and a default admin user.
 * Based on code from https://github.com/Baeldung/spring-security-registration/blob/master/src/main/java/com/baeldung/spring/SetupDataLoader.java 
 */
@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	
	/** True if already setup */
	private boolean alreadySetup = false;

	/** Link to RoleRepository */
	@Autowired
	private RoleRepository roleRepository;
	
	/** Link to UserRepository */
	@Autowired
	private UserRepository userRepository;
	
	/** Link to ItemRepository */
	@Autowired
	private ItemRepository itemRepository;
	
	/** Encodes passwords */
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/** Admin password in application.properties file */
	@Value("${app.admin-user-password}")
	private String adminUserPassword;
	

	/**
	 * When the application loads and the context is refreshed
	 * this method will run and create the admin user role and 
	 * any other user roles defined in the Roles.UserRoles enum.
	 */
	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		
		Role adminRole = createRoleIfNotFound(Roles.ROLE_ADMIN);
		for(Roles.UserRoles role: Roles.UserRoles.values()) {
			createRoleIfNotFound(role.toString());
		}
		
		createUserIfNotFound("Admin User", "admin", "admin@admin.edu", new ArrayList<>(Arrays.asList(adminRole)));
		
		// Create sample items for testing
		createItemIfNotFound("Coffee", "Hot coffee", 5.0);
		createItemIfNotFound("Pastry", "Fresh pastry", 3.0);
		
		alreadySetup = true;
	}
	
	/**
	 * Creates the role with the given name.
	 * @param name role name
	 * @return created role
	 */
	@Transactional
	public Role createRoleIfNotFound(String name) {
		Role role = roleRepository.findByName(name);
		if (role == null) {
			role = new Role();
			role.setName(name);
		}
		role = roleRepository.save(role);
		return role;
		
	}
	
	/**
	 * Creates a user with the given information
	 * @param name user's name
	 * @param username user's username
	 * @param email user's email
	 * @param roles user's roles
	 * @return created user
	 */
	@Transactional
	public User createUserIfNotFound(String name, String username, String email, Collection<Role> roles) {
		Optional<User> returnedUser = userRepository.findByUsernameOrEmail(username, email);
				
		if (returnedUser.isEmpty()) {
			User user = new User();
			user.setName(name);
			user.setUsername(username);
			user.setEmail(email);
			user.setPassword(passwordEncoder.encode(adminUserPassword));
			user.setRoles(roles);
			userRepository.save(user);
			return user;
		} else {
			return returnedUser.get();
		}
		
	}

	/**
	 * Creates an item with the given information if it doesn't already exist
	 * @param name item name
	 * @param description item description
	 * @param price item price
	 * @return created or existing item
	 */
	@Transactional
	public Item createItemIfNotFound(String name, String description, double price) {
		if (itemRepository.existsByName(name)) {
			return null; // Item already exists, skip
		}
		
		Item item = new Item();
		item.setName(name);
		item.setDescription(description);
		item.setPrice(price);
		return itemRepository.save(item);
	}

}
