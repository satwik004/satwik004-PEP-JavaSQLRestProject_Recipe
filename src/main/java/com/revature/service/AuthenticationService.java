package com.revature.service;
import java.util.HashMap;
import java.util.Map;
import com.revature.model.Chef;


/**
 * The AuthenticationService class provides authentication functionality
 * for Chef objects. It manages the login, logout, and registration
 * processes, as well as session management for chefs. This service 
 * utilizes a ChefService to perform operations related to chefs and 
 * maintains a session map to track active sessions.
 */

public class AuthenticationService {

    /**
     * The service used for managing Chef objects and their operations.
     */

    @SuppressWarnings("unused")
    private ChefService chefService;

    /** A map that keeps track of currently logged in users, indexed by session token. */
    public static Map<String, Chef> loggedInUsers = new HashMap<>();

    /**
     * Constructs an AuthenticationService with the specified ChefService and a newly created HashMap for the LoggedInUsers.
     *
     * @param chefService the ChefService to be used by this authentication service
     */
    public AuthenticationService(ChefService chefService) {
        this.chefService = chefService;
        loggedInUsers = new HashMap<>();
    }

    /**
     * TODO: Authenticates a chef by verifying the provided credentials. If successful, a session token is generated and stored in the logged in users map.
     * 
     * @param chef the Chef object containing login credentials
     * @return a session token if the login is successful; null otherwise
     */
    public String login(Chef chef) {
        if (chef == null || chef.getUsername() == null || chef.getPassword() == null) {
            return null;
        }

        // naive auth: find chef by username from available list
        // Since DAO is not directly available, rely on service search by term and exact match
        var possibleMatches = chefService.searchChefs(chef.getUsername());
        for (Chef existing : possibleMatches) {
            if (existing.getUsername().equals(chef.getUsername()) && existing.getPassword().equals(chef.getPassword())) {
                String token = existing.getUsername() + ":" + existing.getPassword();
                loggedInUsers.put(token, existing);
                return token;
            }
        }
        return null; 
    }

    /**
     * TODO: Logs out a chef by removing their session token from the LoggedInUsers map.
     *
     * @param token the session token of the chef to be logged out
     */

    public void logout(String token) {
        if (token != null) {
            loggedInUsers.remove(token);
        }
    }

    /**
	 * TODO: Registers a new chef by saving the chef's information using ChefService.
	 *
	 * @param chef the chef object containing registration details
	 * @return the registered chef object
	 */
    public Chef registerChef(Chef chef) {
        if (chef == null || chef.getUsername() == null) {
            return null;
        }
        // Check if username exists
        var existing = chefService.searchChefs(chef.getUsername());
        boolean usernameTaken = existing.stream().anyMatch(c -> c.getUsername().equals(chef.getUsername()));
        if (usernameTaken) {
            return null;
        }
        chefService.saveChef(chef);
        return chef;
    }

    /**
     * TODO: Retrieves a Chef object from the session token.
     *
     * @param token the session token used to retrieve the chef
     * @return the Chef object associated with the session token; null if not found
     */
    public Chef getChefFromSessionToken(String token) {
        return loggedInUsers.get(token);
    }
}
