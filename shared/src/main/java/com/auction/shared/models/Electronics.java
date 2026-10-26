package com.auction.shared.models;

public class Electronics extends Item {
    private int warrantyMonths;

    // Constructor khởi tạo
    public Electronics(String id, String name, String description, int warrantyMonths) {
        // Gọi super() để truyền id, name, description lên cho lớp cha (Item) xử lý
        super(id, name, description);
        this.warrantyMonths = warrantyMonths;
    }

    // Getter cho thuộc tính riêng
    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    // Ghi đè phương thức abstract của lớp Item
    @Override
    public String getItemType() {
        return "Electronics";
    }
}