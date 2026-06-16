package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.ChiTietGDK;
import com.nhom6.qlbh.model.GiaoDichKho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiaoDichKhoDAO {

    public List<GiaoDichKho> findAll(String loaiGD) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT g.MaGD, g.LoaiGD, g.MaNCC, n.TenNCC, g.MaNV, nv.TenNV, " +
            "g.ThoiGian, g.GhiChu, g.TrangThai, " +
            "COALESCE(agg.SoMH, 0) AS SoMatHang, COALESCE(agg.TongTien, 0) AS TongTien " +
            "FROM GiaoDichKho g " +
            "LEFT JOIN NhaCungCap n  ON n.MaNCC = g.MaNCC " +
            "LEFT JOIN NhanVien nv   ON nv.MaNV = g.MaNV " +
            "LEFT JOIN ( " +
            "    SELECT MaGD, COUNT(*) AS SoMH, SUM(SoLuong * DonGiaNhap) AS TongTien " +
            "    FROM ChiTietGiaoDichKho GROUP BY MaGD " +
            ") agg ON agg.MaGD = g.MaGD");
        if (loaiGD != null && !loaiGD.isEmpty()) sql.append(" WHERE g.LoaiGD = ?");
        sql.append(" ORDER BY g.ThoiGian DESC");

        List<GiaoDichKho> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            if (loaiGD != null && !loaiGD.isEmpty()) ps.setString(1, loaiGD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ChiTietGDK> findChiTiet(String maGD) throws SQLException {
        String sql = "SELECT ct.MaGD, ct.MaSP, sp.TenSP, ct.SoLuong, ct.DonGiaNhap " +
                     "FROM ChiTietGiaoDichKho ct " +
                     "JOIN SanPham sp ON sp.MaSP = ct.MaSP " +
                     "WHERE ct.MaGD = ?";
        List<ChiTietGDK> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maGD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietGDK ct = new ChiTietGDK();
                    ct.setMaGD(rs.getString("MaGD"));
                    ct.setMaSP(rs.getString("MaSP"));
                    ct.setTenSP(rs.getString("TenSP"));
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    public String nextMaGD(String loaiGD) throws SQLException {
        String prefix;
        switch (loaiGD) {
            case "NHAP":     prefix = "PN";  break;
            case "TRA_NHAP": prefix = "THN"; break;
            case "KIEM_KHO": prefix = "KK";  break;
            case "XUAT_HUY": prefix = "XH";  break;
            default:         prefix = "GD";
        }
        int plen = prefix.length();
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(MaGD, ?) AS UNSIGNED)), 0) + 1 " +
                     "FROM GiaoDichKho WHERE MaGD LIKE ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, plen + 1);
            ps.setString(2, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefix + String.format("%03d", rs.getInt(1));
            }
        }
    }

    public boolean existsById(String maGD) throws SQLException {
        String sql = "SELECT 1 FROM GiaoDichKho WHERE MaGD = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maGD);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insert(GiaoDichKho gdk) throws SQLException {
        String sqlH = "INSERT INTO GiaoDichKho (MaGD, LoaiGD, MaNCC, MaNV, ThoiGian, GhiChu, TrangThai) " +
                      "VALUES (?, ?, ?, ?, NOW(), ?, 'HOAN_THANH')";
        String sqlD = "INSERT INTO ChiTietGiaoDichKho (MaGD, MaSP, SoLuong, DonGiaNhap) VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlH)) {
                    ps.setString(1, gdk.getMaGD());
                    ps.setString(2, gdk.getLoaiGD());
                    if (gdk.getMaNCC() != null && !gdk.getMaNCC().isBlank())
                        ps.setString(3, gdk.getMaNCC());
                    else
                        ps.setNull(3, Types.VARCHAR);
                    if (gdk.getMaNV() != null) ps.setInt(4, gdk.getMaNV());
                    else                       ps.setNull(4, Types.INTEGER);
                    ps.setString(5, gdk.getGhiChu());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(sqlD)) {
                    for (ChiTietGDK ct : gdk.getChiTiet()) {
                        ps.setString(1, gdk.getMaGD());
                        ps.setString(2, ct.getMaSP());
                        ps.setInt(3, ct.getSoLuong());
                        ps.setBigDecimal(4, ct.getDonGiaNhap());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private GiaoDichKho mapRow(ResultSet rs) throws SQLException {
        GiaoDichKho g = new GiaoDichKho();
        g.setMaGD(rs.getString("MaGD"));
        g.setLoaiGD(rs.getString("LoaiGD"));
        g.setMaNCC(rs.getString("MaNCC"));
        g.setTenNCC(rs.getString("TenNCC"));
        int maNV = rs.getInt("MaNV");
        g.setMaNV(rs.wasNull() ? null : maNV);
        g.setTenNV(rs.getString("TenNV"));
        Timestamp ts = rs.getTimestamp("ThoiGian");
        if (ts != null) g.setThoiGian(ts.toLocalDateTime());
        g.setGhiChu(rs.getString("GhiChu"));
        g.setTrangThai(rs.getString("TrangThai"));
        g.setSoMatHang(rs.getInt("SoMatHang"));
        g.setTongTien(rs.getBigDecimal("TongTien"));
        return g;
    }
}
