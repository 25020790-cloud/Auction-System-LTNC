package com.auction.shared.utils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// LỚP: PasswordUtils (Utility Class) Tiện ích băm và xác thực mật khẩu dùng chung, tuân thủ nguyên tắc DRY
public final class PasswordUtils {
    // Constructor 'private'
    private PasswordUtils() {}

    /**
     * Mã hóa mật khẩu một chiều (One-way Hashing) bằng thuật toán SHA-256.
     * @param password Mật khẩu thô (plain text) người dùng nhập vào.
     * @return Chuỗi mã hóa Hexadecimal (64 ký tự).
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            return null; // Tránh lỗi NullPointerException khi chuỗi đầu vào rỗng
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Chuyển chuỗi mật khẩu sang mảng byte UTF-8 và tiến hành băm
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            // Chuyển đổi mảng byte kết quả sang chuỗi Hex (Hệ 16) để lưu trữ vào JSON / DB
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Bắt buộc dừng hệ thống hoặc báo lỗi nếu thất bại
            throw new RuntimeException("Lỗi nghiêm trọng khi hash password với SHA-256", e);
        }
    }

    /**
     * Xác thực mật khẩu nhập vào có trùng khớp với mật khẩu đã băm trong file JSON hay không.
     * @param inputPassword Mật khẩu thô người dùng nhập lúc Đăng nhập.
     * @param storedHash Chuỗi băm SHA-256 đã lưu trong cơ sở dữ liệu/JSON.
     * @return true nếu mật khẩu đúng, false nếu sai.
     */
    public static boolean verify(String inputPassword, String storedHash) {
        if (inputPassword == null || storedHash == null) {
            return false;
        }
        // Băm mật khẩu người dùng vừa nhập rồi so sánh trực tiếp với chuỗi băm lưu trữ
        String hashedInput = hash(inputPassword);
        return storedHash.equalsIgnoreCase(hashedInput);
    }
}