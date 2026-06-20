package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.SanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    private static final String BASE_SELECT =
        "SELECT sp.MaSP, sp.TenSP, sp.MaLoai, l.TenLoai, " +
        "sp.GiaVon, sp.GiaBan, sp.TonKho, sp.TrangThai, sp.MucTonToiThieu " +
        "FROM SanPham sp LEFT JOIN LoaiSanPham l ON l.MaLoai = sp.MaLoai ";

    public List<SanPham> findAll() throws SQLException {
        return query(BASE_SELECT + "ORDER BY sp.TenSP", ps -> {});
    }

    public SanPham findById(String maSP) throws SQLException {
        List<SanPham> list = query(BASE_SELECT + "WHERE sp.MaSP = ?", ps -> ps.setString(1, maSP));
        return list.isEmpty() ? null : list.get(0);
    }

    public List<SanPham> search(String keyword, Integer maLoai) throws SQLException {
        String kw = "%" + keyword.trim() + "%";
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE (sp.MaSP LIKE ? OR sp.TenSP LIKE ?)");
        if (maLoai != null) sql.append(" AND sp.MaLoai = ?");
        sql.append(" ORDER BY sp.TenSP");

        return query(sql.toString(), ps -> {
            ps.setString(1, kw);
            ps.setString(2, kw);
            if (maLoai != null) ps.setInt(3, maLoai);
        });
    }

    public boolean existsByMa(String maSP) throws SQLException {
        String sql = "SELECT 1 FROM SanPham WHERE MaSP = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insert(SanPham sp) throws SQLException {
        String sql = "INSERT INTO SanPham (MaSP, TenSP, MaLoai, GiaVon, GiaBan, TonKho, TrangThai) " +
                     "VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSP());
            ps.setString(2, sp.getTenSP());
            if (sp.getMaLoai() != null) ps.setInt(3, sp.getMaLoai()); else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, sp.getGiaVon());
            ps.setBigDecimal(5, sp.getGiaBan());
            ps.setInt(6, sp.getTrangThai());
            ps.executeUpdate();
        }
    }

    public void update(SanPham sp) throws SQLException {
        String sql = "UPDATE SanPham SET TenSP=?, MaLoai=?, GiaVon=?, GiaBan=?, TrangThai=? WHERE MaSP=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sp.getTenSP());
            if (sp.getMaLoai() != null) ps.setInt(2, sp.getMaLoai()); else ps.setNull(2, Types.INTEGER);
            ps.setBigDecimal(3, sp.getGiaVon());
            ps.setBigDecimal(4, sp.getGiaBan());
            ps.setInt(5, sp.getTrangThai());
            ps.setString(6, sp.getMaSP());
            ps.executeUpdate();
        }
    }

    public void delete(String maSP) throws SQLException {
        String sql = "DELETE FROM SanPham WHERE MaSP = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSP);
            ps.executeUpdate();
        }
    }

    public void updateMucTon(String maSP, int mucTon) throws SQLException {
        String sql = "UPDATE SanPham SET MucTonToiThieu = ? WHERE MaSP = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mucTon);
            ps.setString(2, maSP);
            ps.executeUpdate();
        }
    }

    public List<SanPham> findSapHet() throws SQLException {
        return query(BASE_SELECT +
            "WHERE sp.TrangThai = 1 AND sp.TonKho <= sp.MucTonToiThieu ORDER BY sp.TonKho", ps -> {});
    }

    public int countSapHet() throws SQLException {
        String sql = "SELECT COUNT(*) FROM SanPham WHERE TrangThai = 1 AND TonKho <= MucTonToiThieu";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    // ---- helper ----
    @FunctionalInterface
    interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<SanPham> query(String sql, ParamSetter setter) throws SQLException {
        List<SanPham> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private SanPham mapRow(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSP(rs.getString("MaSP"));
        sp.setTenSP(rs.getString("TenSP"));
        int maLoai = rs.getInt("MaLoai");
        sp.setMaLoai(rs.wasNull() ? null : maLoai);
        sp.setTenLoai(rs.getString("TenLoai"));
        sp.setGiaVon(rs.getBigDecimal("GiaVon"));
        sp.setGiaBan(rs.getBigDecimal("GiaBan"));
        sp.setTonKho(rs.getInt("TonKho"));
        sp.setTrangThai(rs.getInt("TrangThai"));
        sp.setMucTonToiThieu(rs.getInt("MucTonToiThieu"));
        return sp;
    }
}
