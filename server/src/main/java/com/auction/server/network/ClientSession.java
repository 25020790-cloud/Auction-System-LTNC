package com.auction.server.network;

import com.auction.shared.models.Role;
import com.auction.shared.models.User;

/** Authentication state scoped to one TCP connection. */
public final class ClientSession {
    private String userId;
    private Role role;

    public synchronized void authenticate(User user) {
        if (user == null || user.getId() == null || user.getRole() == null) {
            clear();
            return;
        }
        userId = user.getId();
        role = user.getRole();
    }

    public synchronized String getUserId() {
        return userId;
    }

    public synchronized Role getRole() {
        return role;
    }

    public synchronized boolean isAuthenticated() {
        return userId != null && role != null;
    }

    public synchronized void clear() {
        userId = null;
        role = null;
    }
}
