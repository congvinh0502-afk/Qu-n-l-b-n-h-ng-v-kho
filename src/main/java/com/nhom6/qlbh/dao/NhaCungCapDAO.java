package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.NhaCungCap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAO {

    private static final String BASE_SELECT =
        "SELECT MaNCC, TenNCC, DienThoai, DiaChi FROM NhaCungCap ";

    public List<NhaCungCap> findAll() throws SQLException {
        return query(BASE_SELECT + "ORDER BY TenNCC", ps -> {});
    }

    public List<NhaCungCap> search(String keyword) throws SQLException {
        String kw = "%" + keyword.trim() + "%";
        return query(BASE_SELECT + "WHERE MaNCC LIKE ? OR TenNCC LIKE ? OR DienThoai LIKE ? ORDER BY TenNCC",
            ps -> { ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); });
    }

    public boolean existsByMa(String maNCC) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM NhaCungCap WHERE MaNCC = ?")) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insert(NhaCungCap ncc) throws SQLException {
        String sql = "INSERT INTO NhaCungCap (MaNCC, TenNCC, DienThoai, DiaChi) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ncc.getMaNCC());
            ps.setString(2, ncc.getTenNCC());
            ps.setString(3, ncc.getDienThoai());
            ps.setString(4, ncc.getDiaChi());
            ps.executeUpdate();
        }
    }

    public void update(NhaCungCap ncc) throws SQLException {
        String sql = "UPDATE NhaCungCap SET TenNCC=?, DienThoai=?, DiaChi=? WHERE MaNCC=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getDienThoai());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getMaNCC());
            ps.executeUpdate();
        }
    }

    public void delete(String maNCC) throws SQLException {
        String sql = "DELETE FROM NhaCungCap WHERE MaNCC = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<NhaCungCap> query(String sql, ParamSetter setter) throws SQLException {
        List<NhaCungCap> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNCC(rs.getString("MaNCC"));
                    ncc.setTenNCC(rs.getString("TenNCC"));
                    ncc.setDienThoai(rs.getString("DienThoai"));
                    ncc.setDiaChi(rs.getString("DiaChi"));
                    list.add(ncc);
                }
            }
        }
        return list;
    }
}
