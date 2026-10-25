package com.auction.shared.models;

public class Admin extends User {
    private String permissions; // Quyền hạn riêng của Admin
    // Constructor không tham số
    public Admin() {
        super();
        this.role = Role.ADMIN;
    }
    // Constructor đầy đủ tham số
    public Admin(String username, String password, String email, String fullName, String permissions) {
        super(username, password, email, fullName, Role.ADMIN);
        this.permissions = permissions;
    }
    // GETTERS & SETTERS
    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    // Ghi đè phương thức printInfo() từ lớp cha User (Polymorphism)
    public void printInfo() {
        System.out.printf("[ADMIN] %s (%s) | Quyền: %s%n",
                getFullName(), getUsername(), permissions);
    }
}