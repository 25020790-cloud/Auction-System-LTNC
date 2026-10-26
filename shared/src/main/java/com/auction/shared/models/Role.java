package com.auction.shared.models;
//Enum định nghĩa các vai trò trong hệ thống đấu giá kèm tên hiển thị
public enum Role {
    ADMIN("Quản trị viên"),
    SELLER("Người bán"),
    BIDDER("Người đấu giá");
    private final String displayName;
    // Constructor của Enum
    Role(String displayName) {
        this.displayName = displayName;
    }
    // Getter lấy tên hiển thị tiếng Việt trên giao diện GUI
    public String getDisplayName() {
        return displayName;
    }
}
