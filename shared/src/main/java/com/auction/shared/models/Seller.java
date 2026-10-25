package com.auction.shared.models;
import java.math.BigDecimal;
public class Seller extends User {
    private double rating; // Điểm đánh giá uy tín của người bán

    //Biến ví tiền cho Seller
    private BigDecimal balance = BigDecimal.ZERO;

    // Constructor không tham số để thư viện Gson có thể chuyển đổi dữ liệu từ file JSON thành Object Java.
    public Seller() {
        super();
        this.role = Role.SELLER;
    }

    //Constructor đầy đủ tham số
    public Seller(String username, String password, String email, String fullName, double rating) {
        super(username, password, email, fullName, Role.SELLER);
        this.rating = rating;
    }

    // GETTERS & SETTERS
    public double getRating() {return rating;}
    public void setRating(double rating) {this.rating = rating;}

    @Override
    public BigDecimal getBalance() {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // Cập nhật số dư tài khoản cho Seller.
    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    // LOGIC NGHIỆP VỤ NGƯỜI BÁN

    // chỉ có Seller mới được phép đăng bán sản phẩm.
    @Override
    public boolean canSell() {
        return true;
    }

    // In thông tin Người bán ra màn hình Console kèm điểm rating và số dư.
    public void printInfo() {
        System.out.printf("[SELLER] %s (%s) | Rating: %.1f | Số dư: %s%n",
                getFullName(), getUsername(), rating, getBalance());
    }
}