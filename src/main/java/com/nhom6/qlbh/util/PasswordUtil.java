package com.nhom6.qlbh.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /** Chạy tạm để sinh BCrypt hash cho sample_data — xóa main() sau khi dùng xong. */
    public static void main(String[] args) {
        String[] users    = {"admin", "bac"};
        String[] passwords = {"admin123", "bac123"};
        for (int i = 0; i < users.length; i++) {
            System.out.printf("UPDATE TaiKhoan SET MatKhau='%s' WHERE TenDangNhap='%s';%n",
                    hash(passwords[i]), users[i]);
        }
    }
}
