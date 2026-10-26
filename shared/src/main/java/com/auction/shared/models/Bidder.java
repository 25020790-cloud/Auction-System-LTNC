package com.auction.shared.models;

import java.math.BigDecimal;
import java.util.Objects;
public class Bidder extends User {
    private BigDecimal balance;            // Số dư tổng tài khoản
    private BigDecimal maxBidAmount;       // Ngưỡng giá đặt tối đa cho tính năng Auto-Bid
    private BigDecimal frozenBalance = BigDecimal.ZERO;// Số dư bị đóng băng khi đang tham gia đấu giá
    // Constructor không tham số để thư viện Gson có thể chuyển đổi dữ liệu từ file JSON thành Object Java.
    public Bidder() {
        super();
        this.role = Role.BIDDER;
    }

    //Constructor đầy đủ tham số
    public Bidder(String username, String password, String email, String fullName, BigDecimal balance) {
        super(username, password, email, fullName, Role.BIDDER);
        this.balance = Objects.requireNonNull(balance, "Không được để trống số dư");
    }

    // GETTERS & SETTERS

    //Lấy tổng số dư tài khoản dùng synchronized để đảm bảo an toàn đa luồng
    @Override
    public synchronized BigDecimal getBalance() {
        return balance;
    }

    // Cập nhật lại số dư tài khoản
    public synchronized void setBalance(BigDecimal balance) {
        this.balance = Objects.requireNonNull(balance, "Không được để trống số dư");
    }

    // Lấy giá trị đấu giá tự động tối đa
    public BigDecimal getMaxBidAmount() {
        return maxBidAmount;
    }

    // Cập nhật giá trị đấu giá tự động tối đa
    public void setMaxBidAmount(BigDecimal maxBidAmount) {
        this.maxBidAmount = Objects.requireNonNull(maxBidAmount, "Không được để trống số tiền đấu giá tối đa");
    }

    // LOGIC NGHIỆP VỤ ĐẤU GIÁ

    //In thông tin người đấu giá ra màn hình Console
    @Override
    public void printInfo() {
        System.out.printf("[BIDDER] %s (%s) | Số dư: %s%n",
                getFullName(), getUsername(), balance);
    }

    //cho phép đặt giá
    @Override
    public boolean canBid() {
        return true;
    }

    //Giả lập tính năng đặt giá tự động
    public void placeAutoBid() {
        System.out.println("⚙️ Kích hoạt Auto-Bid cho " + getUsername() + " với ngưỡng chịu đựng tối đa: " + this.maxBidAmount);
    }

    // Tính số tiền thực tế có thể sử dụng (Khả dụng = Tổng tiền - Tiền đang bị đóng băng)
    public synchronized BigDecimal getAvailableBalance() {
        if (frozenBalance == null) frozenBalance = BigDecimal.ZERO;
        if (balance == null) balance = BigDecimal.ZERO;
        return balance.subtract(frozenBalance);
    }

    // Đóng băng (giam) tiền đặt cọc khi Bidder đặt giá thành công
    public synchronized boolean freezeMoney(BigDecimal amount) {
        if (frozenBalance == null) frozenBalance = BigDecimal.ZERO;
        BigDecimal available = getAvailableBalance();
        if (amount.compareTo(available) > 0) {
            return false;
        }
        this.frozenBalance = this.frozenBalance.add(amount);
        return true;
    }

    // Mở khóa (hoàn lại) tiền khi Bidder bị người khác trả giá cao hơn
    public synchronized void unfreezeMoney(BigDecimal amount) {
        if (frozenBalance == null) frozenBalance = BigDecimal.ZERO;
        this.frozenBalance = this.frozenBalance.subtract(amount).max(BigDecimal.ZERO);
    }
}
