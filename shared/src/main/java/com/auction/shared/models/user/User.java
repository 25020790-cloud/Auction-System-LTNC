package com.auction.shared.models;

import com.auction.shared.models.base.Entity;
import java.math.BigDecimal;

public abstract class User extends Entity {
    private String username;
    private String password;
    private String email;
    private String fullName;
    protected Role role;

    // Constructor không tham số (Bắt buộc cho Gson)
    public User() {
        super();
    }

    // Constructor có tham số cơ bản
    public User(String username, String password, String email, String fullName) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    // Constructor đầy đủ tham số
    public User(String username, String password, String email, String fullName, Role role) {
        this(username, password, email, fullName);
        this.role = role;
    }

    // GETTERS & SETTERS

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BigDecimal getBalance() { return BigDecimal.ZERO; }
    public double getBalanceAsDouble() { return getBalance().doubleValue(); }

    public Role getRole() { return this.role; }
    public void setRole(Role role) { this.role = role; }

    // Các hàm kiểm tra quyền hạn (Polymorphism)
    public boolean canBid()  { return false; }
    public boolean canSell() { return false; }

    @Override
    public abstract void printInfo();
}