package com.auction.client.session;

public class SessionContext {
    // 1. Áp dụng Singleton Pattern (Khởi tạo lười - Lazy Initialization)
    private static SessionContext instance;
    
    // Các thuộc tính lưu trữ thông tin người dùng đang đăng nhập
    private String currentUserId;
    private String currentUsername;

    // 2. Private constructor
    private SessionContext() {
    }

    // 3. Hàm lấy instance duy nhất
    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    // Hàm gọi khi người dùng đăng nhập thành công
    public void login(String userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
    }

    // Hàm gọi khi người dùng đăng xuất
    public void logout() {
        this.currentUserId = null;
        this.currentUsername = null;
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
}