package com.auction.shared.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// LỚP: PasswordUtils (Utility Class) Tiện ích băm và xác thực mật khẩu dùng chung, tuân thủ nguyên tắc DRY
public final class PasswordUtils {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Constructor 'private'
    private PasswordUtils() {}

    /**
     * Hash mật khẩu bằng PBKDF2-HMAC-SHA256 với salt ngẫu nhiên.
     * @param password Mật khẩu thô (plain text) người dùng nhập vào.
     * @return Chuỗi lưu trữ gồm thuật toán, số vòng lặp, salt và hash.
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            return null; // Tránh lỗi NullPointerException khi chuỗi đầu vào rỗng
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(hash);
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
        try {
            String[] parts = storedHash.split("\\$", -1);
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1) {
                return false;
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] salt = decoder.decode(parts[2]);
            byte[] expectedHash = decoder.decode(parts[3]);
            byte[] actualHash = derive(inputPassword, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Cannot hash password", exception);
        } finally {
            keySpec.clearPassword();
        }
    }
}
