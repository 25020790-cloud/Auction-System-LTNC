package com.auction.shared.utils;

import com.google.gson.Gson;

public class GsonParser {
    // 1. Tạo sẵn một instance duy nhất của Gson (Singleton)
    private static final Gson instance = new Gson();

    // 2. Để private constructor để ngăn chặn các class khác dùng từ khóa 'new'
    private GsonParser() {
    }

    // 3. Cung cấp một hàm public static để mọi nơi có thể lấy instance này ra dùng
    public static Gson getInstance() {
        return instance;
    }
}