package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    private static final String BASE_SELECT =
        "SELECT MaKH, TenKH, DienThoai, TongBan, TongBanTruTra FROM KhachHang ";

    public List<KhachHang> findAll() throws SQLException {
        return query(BASE_SELECT + "ORDER BY TenKH", ps -> {});
    }

    public List<KhachHang> search(String keyword) throws SQLException {
        String kw = "%" + keyword.trim() + "%";
        return query(BASE_SELECT + "WHERE MaKH LIKE ? OR TenKH LIKE ? OR DienThoai LIKE ? ORDER BY TenKH",
            ps -> { ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); });
    }

    public boolean existsByMa(String maKH) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM KhachHang WHERE MaKH = ?")) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insert(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (MaKH, TenKH, DienThoai) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getDienThoai());
            ps.executeUpdate();
        }
    }

    public void update(KhachHang kh) throws SQLException {
        String sql = "UPDATE KhachHang SET TenKH=?, DienThoai=? WHERE MaKH=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getDienThoai());
            ps.setString(3, kh.getMaKH());
            ps.executeUpdate();
        }
    }

    public void delete(String maKH) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE MaKH = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<KhachHang> query(String sql, ParamSetter setter) throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("MaKH"));
                    kh.setTenKH(rs.getString("TenKH"));
                    kh.setDienThoai(rs.getString("DienThoai"));
                    kh.setTongBan(rs.getBigDecimal("TongBan"));
                    kh.setTongBanTruTra(rs.getBigDecimal("TongBanTruTra"));
                    list.add(kh);
                }
            }
        }
        return list;
    }
}
