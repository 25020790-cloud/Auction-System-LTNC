package com.auction.client.session;

public class SessionContext {
    // 1. Áp dụng Singleton Pattern (Khởi tạo lười - Lazy Initialization)
    private static final SessionContext INSTANCE = new SessionContext();
    
    // Các thuộc tính lưu trữ thông tin người dùng đang đăng nhập
    private volatile String currentUserId;
    private volatile String currentUsername;
    private volatile String currentRole;

    // 2. Private constructor
    private SessionContext() {
    }

    // 3. Hàm lấy instance duy nhất
    public static SessionContext getInstance() {
        return INSTANCE;
    }

    // Hàm gọi khi người dùng đăng nhập thành công
    public void login(String userId, String username) {
        login(userId, username, "BIDDER");
    }

    public void login(String userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
    }

    // Hàm gọi khi người dùng đăng xuất
    public void logout() {
        this.currentUserId = null;
        this.currentUsername = null;
        this.currentRole = null;
    }

    // Kiểm tra xem đã đăng nhập chưa
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    // Getters để lấy thông tin sử dụng ở các giao diện khác
    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentRole() {
        return currentRole;
    }
}
