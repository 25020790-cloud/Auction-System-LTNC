package com.auction.server.managers;

import com.auction.shared.models.Bidder;
import com.auction.shared.models.Admin;
import com.auction.shared.models.Role;
import com.auction.shared.models.Seller;
import com.auction.shared.models.User;
import com.auction.shared.utils.PasswordUtils;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.UserDao.UserRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps accounts in memory and persists account changes through UserDao. */
public class UserManager {
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final UserDao userDao;

    public UserManager() {
        this(new UserDao());
    }

    public UserManager(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("User DAO is required");
        }
        this.userDao = userDao;
        for (UserRecord record : userDao.loadAll()) {
            User user = fromRecord(record);
            usersByUsername.put(normalize(user.getUsername()), user);
        }
    }

    public synchronized boolean register(String username, String password,
                                         String email, Role role) {
        if (username == null || username.isBlank() || password == null || password.isBlank()
                || email == null || email.isBlank() || role == null || role == Role.ADMIN) {
            return false;
        }

        String normalizedUsername = normalize(username);
        if (usersByUsername.containsKey(normalizedUsername)) {
            return false;
        }

        String passwordHash = PasswordUtils.hash(password);
        User user = role == Role.SELLER
                ? new Seller(username.trim(), passwordHash, email.trim(), username.trim(), 0)
                : new Bidder(username.trim(), passwordHash, email.trim(), username.trim(), BigDecimal.ZERO);
        usersByUsername.put(normalizedUsername, user);
        try {
            userDao.saveAll(toRecords());
        } catch (RuntimeException exception) {
            usersByUsername.remove(normalizedUsername, user);
            throw exception;
        }
        return true;
    }

    public User authenticate(String username, String password, Role requestedRole) {
        if (username == null || password == null || requestedRole == null) {
            return null;
        }

        User user = usersByUsername.get(normalize(username));
        if (user == null || user.getRole() != requestedRole
                || !PasswordUtils.verify(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private List<UserRecord> toRecords() {
        List<UserRecord> records = new ArrayList<>();
        for (User user : usersByUsername.values()) {
            records.add(new UserRecord(user.getUsername(), user.getPassword(),
                    user.getEmail(), user.getFullName(), user.getRole(), user.getBalance()));
        }
        return records;
    }

    private User fromRecord(UserRecord record) {
        if (record == null || record.getUsername() == null || record.getRole() == null) {
            throw new IllegalStateException("User data contains an invalid account record");
        }
        return switch (record.getRole()) {
            case SELLER -> {
                Seller seller = new Seller(record.getUsername(), record.getPasswordHash(),
                        record.getEmail(), record.getFullName(), 0);
                seller.setBalance(record.getBalance());
                yield seller;
            }
            case BIDDER -> new Bidder(record.getUsername(), record.getPasswordHash(),
                    record.getEmail(), record.getFullName(), record.getBalance());
            case ADMIN -> new Admin(record.getUsername(), record.getPasswordHash(),
                    record.getEmail(), record.getFullName(), "");
        };
    }
}
