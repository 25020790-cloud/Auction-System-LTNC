package com.auction.client.cache;

import java.util.ArrayList;
import java.util.List;

public class BidHistoryCache {
    // 1. Áp dụng Singleton Pattern
    private static BidHistoryCache instance;
    
    // Danh sách lưu trữ các dòng lịch sử đấu giá
    private List<String> historyList;

    // 2. Private constructor
    private BidHistoryCache() {
        historyList = new ArrayList<>();
    }

    // 3. Hàm lấy instance duy nhất
    public static BidHistoryCache getInstance() {
        if (instance == null) {
            instance = new BidHistoryCache();
        }
        return instance;
    }

    // Thêm một lượt đấu giá mới vào lịch sử
    public void addHistory(String record) {
        historyList.add(record);
    }

    // Lấy toàn bộ lịch sử để hiển thị lên màn hình
    public List<String> getHistory() {
        return historyList;
    }
    
    // Xóa sạch lịch sử (dùng khi người dùng đăng xuất)
    public void clearCache() {
        historyList.clear();
    }
}