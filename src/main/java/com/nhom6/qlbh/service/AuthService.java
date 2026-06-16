package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.TaiKhoanDAO;
import com.nhom6.qlbh.model.TaiKhoan;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private static TaiKhoan currentUser;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public TaiKhoan login(String username, String password) throws SQLException {
        TaiKhoan tk = dao.findByUsername(username);
        if (tk == null) return null;
        if (!BCrypt.checkpw(password, tk.getMatKhau())) return null;
        currentUser = tk;
        return tk;
    }

    public static TaiKhoan getCurrentUser() { return currentUser; }

    public static void logout() { currentUser = null; }
}
