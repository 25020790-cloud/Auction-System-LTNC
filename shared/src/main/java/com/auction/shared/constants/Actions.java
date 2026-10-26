package com.auction.shared.constants;

public final class Actions {
    private Actions() {
        // Chặn khởi tạo instance
    }

    // Xác thực tài khoản
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER = "REGISTER";
    public static final String REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";

    // Nghiệp vụ đấu giá
    public static final String BID = "BID";
    public static final String BID_SUCCESS = "BID_SUCCESS";
    public static final String BID_FAILED = "BID_FAILED";
    public static final String AUCTION_UPDATE = "AUCTION_UPDATE";
    public static final String GET_AUCTIONS = "GET_AUCTIONS";
    public static final String AUCTION_LIST = "AUCTION_LIST";
    public static final String GET_MY_BID_HISTORY = "GET_MY_BID_HISTORY";
    public static final String MY_BID_HISTORY = "MY_BID_HISTORY";

    // Hệ thống & Kết nối
    public static final String ERROR = "ERROR";
    public static final String BROADCAST = "BROADCAST";
    public static final String DISCONNECT = "DISCONNECT";
}
