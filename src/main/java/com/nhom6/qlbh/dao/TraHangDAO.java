package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.ChiTietTH;
import com.nhom6.qlbh.model.TraHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TraHangDAO {

    private static final String BASE_SELECT =
        "SELECT th.MaTra, th.MaHD, th.MaKH, kh.TenKH, th.MaNV, nv.TenNV, " +
        "th.ThoiGian, th.TongTienHang, th.CanTraKhach, th.DaTraKhach, th.LyDo, th.TrangThai " +
        "FROM TraHang th " +
        "LEFT JOIN KhachHang kh ON kh.MaKH = th.MaKH " +
        "LEFT JOIN NhanVien  nv ON nv.MaNV  = th.MaNV ";

    public List<TraHang> findAll() throws SQLException {
        List<TraHang> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(BASE_SELECT + "ORDER BY th.ThoiGian DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<ChiTietTH> findChiTiet(String maTra) throws SQLException {
        String sql = "SELECT ct.MaTra, ct.MaSP, sp.TenSP, ct.SoLuong, ct.DonGia " +
                     "FROM ChiTietTraHang ct JOIN SanPham sp ON sp.MaSP = ct.MaSP WHERE ct.MaTra = ?";
        List<ChiTietTH> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maTra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietTH ct = new ChiTietTH();
                    ct.setMaTra(rs.getString("MaTra")); ct.setMaSP(rs.getString("MaSP"));
                    ct.setTenSP(rs.getString("TenSP")); ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGia(rs.getBigDecimal("DonGia")); list.add(ct);
                }
            }
        }
        return list;
    }

    public String nextMaTra() throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(MaTra, 3) AS UNSIGNED)), 0) + 1 FROM TraHang WHERE MaTra LIKE 'TH%'";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return "TH" + String.format("%03d", rs.getInt(1));
        }
    }

    public void insert(TraHang th) throws SQLException {
        String sqlH = "INSERT INTO TraHang (MaTra, MaHD, MaKH, MaNV, ThoiGian, TongTienHang, CanTraKhach, DaTraKhach, LyDo, TrangThai) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, 'DA_TRA')";
        String sqlD = "INSERT INTO ChiTietTraHang (MaTra, MaSP, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlH)) {
                    ps.setString(1, th.getMaTra());
                    if (th.getMaHD() != null && !th.getMaHD().isBlank()) ps.setString(2, th.getMaHD());
                    else ps.setNull(2, Types.VARCHAR);
                    if (th.getMaKH() != null && !th.getMaKH().isBlank()) ps.setString(3, th.getMaKH());
                    else ps.setNull(3, Types.VARCHAR);
                    if (th.getMaNV() != null) ps.setInt(4, th.getMaNV());
                    else ps.setNull(4, Types.INTEGER);
                    ps.setBigDecimal(5, th.getTongTienHang());
                    ps.setBigDecimal(6, th.getCanTraKhach());
                    ps.setBigDecimal(7, th.getDaTraKhach());
                    ps.setString(8, th.getLyDo());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(sqlD)) {
                    for (ChiTietTH ct : th.getChiTiet()) {
                        ps.setString(1, th.getMaTra()); ps.setString(2, ct.getMaSP());
                        ps.setInt(3, ct.getSoLuong()); ps.setBigDecimal(4, ct.getDonGia());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    private TraHang mapRow(ResultSet rs) throws SQLException {
        TraHang th = new TraHang();
        th.setMaTra(rs.getString("MaTra")); th.setMaHD(rs.getString("MaHD"));
        th.setMaKH(rs.getString("MaKH")); th.setTenKH(rs.getString("TenKH"));
        int maNV = rs.getInt("MaNV"); th.setMaNV(rs.wasNull() ? null : maNV);
        th.setTenNV(rs.getString("TenNV"));
        Timestamp ts = rs.getTimestamp("ThoiGian");
        if (ts != null) th.setThoiGian(ts.toLocalDateTime());
        th.setTongTienHang(rs.getBigDecimal("TongTienHang"));
        th.setCanTraKhach(rs.getBigDecimal("CanTraKhach"));
        th.setDaTraKhach(rs.getBigDecimal("DaTraKhach"));
        th.setLyDo(rs.getString("LyDo")); th.setTrangThai(rs.getString("TrangThai"));
        return th;
    }
}
