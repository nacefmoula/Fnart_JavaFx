package tn.esprit.utils;

import tn.esprit.models.User;

/**
 * Utility class to manage the current user session
 */
public class SessionManager {
    private static User currentUser;

    /**
     * Set the current logged-in user
     * @param user The user who just logged in
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Get the current logged-in user
     * @return The currently logged-in user or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear the current user session (logout)
     */
    public static void clearSession() {
        currentUser = null;
    }
}