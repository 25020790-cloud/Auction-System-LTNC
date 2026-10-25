package com.auction.shared.factory;

// Import các model từ package models để sử dụng
import com.auction.shared.models.Art;
import com.auction.shared.models.Electronics;
import com.auction.shared.models.Item;
import com.auction.shared.models.Vehicle;

public class ItemFactory {

    /**
     * Factory Method: Tự động quyết định tạo ra đối tượng nào (Electronics, Art, Vehicle)
     * dựa trên tham số 'type' truyền vào.
     */
    public static Item createItem(String type, String id, String name, String description, String extraInfo) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Loại sản phẩm không được để trống!");
        }

        // Chuyển type về chữ thường để so sánh cho an toàn
        switch (type.toLowerCase()) {
            case "electronics":
                // extraInfo của đồ điện tử là số tháng bảo hành (cần ép kiểu sang số nguyên)
                int warranty = Integer.parseInt(extraInfo);
                return new Electronics(id, name, description, warranty);
                
            case "art":
                // extraInfo của nghệ thuật là tên tác giả
                return new Art(id, name, description, extraInfo);
                
            case "vehicle":
                // extraInfo của xe cộ là hãng xe
                return new Vehicle(id, name, description, extraInfo);
                
            default:
                throw new IllegalArgumentException("Nhà máy không hỗ trợ tạo loại sản phẩm: " + type);
        }
    }
}
