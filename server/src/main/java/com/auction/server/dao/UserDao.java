package com.auction.server.dao;

import com.auction.shared.models.Role;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/** Persists user account records without relying on polymorphic Gson adapters. */
public class UserDao {
    private final Path file;

    public UserDao() {
        this(JsonDao.dataFile("users.json"));
    }

    public UserDao(Path file) {
        this.file = file;
    }

    public synchronized List<UserRecord> loadAll() {
        return JsonDao.readList(file, UserRecord.class);
    }

    public synchronized void saveAll(List<UserRecord> users) {
        JsonDao.writeList(file, users);
    }

    public static class UserRecord {
        private String username;
        private String passwordHash;
        private String email;
        private String fullName;
        private Role role;
        private BigDecimal balance = BigDecimal.ZERO;

        public UserRecord() {
        }

        public UserRecord(String username, String passwordHash, String email,
                          String fullName, Role role, BigDecimal balance) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.balance = balance == null ? BigDecimal.ZERO : balance;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public Role getRole() {
            return role;
        }

        public BigDecimal getBalance() {
            return balance == null ? BigDecimal.ZERO : balance;
        }
    }
}
