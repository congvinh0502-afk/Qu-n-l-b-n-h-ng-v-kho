package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.NhanVien;
import com.nhom6.qlbh.model.TaiKhoan;
import com.nhom6.qlbh.model.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    public TaiKhoan findByUsername(String username) throws SQLException {
        String sql = "SELECT tk.MaTK, tk.TenDangNhap, tk.MatKhau, tk.VaiTro, " +
                     "nv.MaNV, nv.TenNV, nv.DienThoai, nv.ChucVu " +
                     "FROM TaiKhoan tk " +
                     "LEFT JOIN NhanVien nv ON nv.MaNV = tk.MaNV " +
                     "WHERE tk.TenDangNhap = ?";

        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private TaiKhoan mapRow(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(rs.getInt("MaTK"));
        tk.setTenDangNhap(rs.getString("TenDangNhap"));
        tk.setMatKhau(rs.getString("MatKhau"));
        tk.setVaiTro(VaiTro.valueOf(rs.getString("VaiTro")));

        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setTenNV(rs.getString("TenNV"));
        nv.setDienThoai(rs.getString("DienThoai"));
        nv.setChucVu(rs.getString("ChucVu"));
        tk.setNhanVien(nv);

        return tk;
    }
}
