package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.ThanhToan;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThanhToanDAO {

    public List<ThanhToan> findByHoaDon(String maHD) throws SQLException {
        String sql = "SELECT MaTT, MaHD, SoTien, HinhThuc, ThoiGian FROM ThanhToan WHERE MaHD = ? ORDER BY ThoiGian";
        List<ThanhToan> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThanhToan tt = new ThanhToan();
                    tt.setMaTT(rs.getInt("MaTT")); tt.setMaHD(rs.getString("MaHD"));
                    tt.setSoTien(rs.getBigDecimal("SoTien")); tt.setHinhThuc(rs.getString("HinhThuc"));
                    Timestamp ts = rs.getTimestamp("ThoiGian");
                    if (ts != null) tt.setThoiGian(ts.toLocalDateTime());
                    list.add(tt);
                }
            }
        }
        return list;
    }

    /** Insert ThanhToan + update HoaDon.DaThanhToan + TrangThai in one transaction */
    public void insert(String maHD, BigDecimal soTien, String hinhThuc) throws SQLException {
        String sqlTT = "INSERT INTO ThanhToan (MaHD, SoTien, HinhThuc, ThoiGian) VALUES (?, ?, ?, NOW())";
        String sqlHD = "UPDATE HoaDon SET DaThanhToan = DaThanhToan + ?, " +
                       "TrangThai = CASE WHEN DaThanhToan + ? >= TongSauGiamGia THEN 'DA_TT' ELSE 'CHUA_TT' END " +
                       "WHERE MaHD = ?";
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlTT)) {
                    ps.setString(1, maHD); ps.setBigDecimal(2, soTien); ps.setString(3, hinhThuc);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(sqlHD)) {
                    ps.setBigDecimal(1, soTien); ps.setBigDecimal(2, soTien); ps.setString(3, maHD);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }
}
