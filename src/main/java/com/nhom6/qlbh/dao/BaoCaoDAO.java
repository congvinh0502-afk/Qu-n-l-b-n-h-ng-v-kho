package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.DoanhThuNgay;
import com.nhom6.qlbh.model.PhanTichSanPham;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaoCaoDAO {

    /** Doanh thu theo ngày trong khoảng [from, to] */
    public List<DoanhThuNgay> getDoanhThuNgay(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT Ngay, DoanhThuThuan, SoHoaDon FROM v_doanhthu_ngay " +
                     "WHERE Ngay BETWEEN ? AND ? ORDER BY Ngay";
        List<DoanhThuNgay> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DoanhThuNgay(
                    rs.getDate("Ngay").toLocalDate(),
                    rs.getBigDecimal("DoanhThuThuan"),
                    rs.getInt("SoHoaDon")
                ));
            }
        }
        return list;
    }

    /** Top N sản phẩm, sắp xếp theo cột chỉ định (DoanhThu / SoLuongThuc / LoiNhuanGop) */
    public List<PhanTichSanPham> getTopSanPham(int limit, String orderBy) throws SQLException {
        // orderBy is controlled internally — no injection risk
        String col = switch (orderBy) {
            case "SoLuongThuc" -> "SoLuongThuc";
            case "LoiNhuanGop" -> "LoiNhuanGop";
            default            -> "DoanhThu";
        };
        String sql = "SELECT MaSP, TenSP, SoLuongBan, SoLuongTra, SoLuongThuc, DoanhThu, LoiNhuanGop " +
                     "FROM v_phantich_sanpham ORDER BY " + col + " DESC LIMIT ?";
        return querySanPham(sql, limit);
    }

    /** Toàn bộ bảng phân tích sản phẩm, sắp theo doanh thu */
    public List<PhanTichSanPham> getAllPhanTich() throws SQLException {
        String sql = "SELECT MaSP, TenSP, SoLuongBan, SoLuongTra, SoLuongThuc, DoanhThu, LoiNhuanGop " +
                     "FROM v_phantich_sanpham ORDER BY DoanhThu DESC";
        return querySanPham(sql, null);
    }

    private List<PhanTichSanPham> querySanPham(String sql, Integer limit) throws SQLException {
        List<PhanTichSanPham> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (limit != null) ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PhanTichSanPham(
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getInt("SoLuongBan"),
                    rs.getInt("SoLuongTra"),
                    rs.getInt("SoLuongThuc"),
                    rs.getBigDecimal("DoanhThu"),
                    rs.getBigDecimal("LoiNhuanGop")
                ));
            }
        }
        return list;
    }

    /** 4 chỉ số KPI tổng hợp */
    public Map<String, Object> getKpiTongQuan() throws SQLException {
        Map<String, Object> kpi = new HashMap<>();
        // Tổng doanh thu và số hóa đơn từ HoaDon
        String sqlHD = "SELECT COALESCE(SUM(TongSauGiamGia), 0) AS TongDT, COUNT(*) AS SoHD FROM HoaDon";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sqlHD);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                kpi.put("tongDoanhThu", rs.getBigDecimal("TongDT"));
                kpi.put("soHoaDon",     rs.getLong("SoHD"));
            }
        }
        // Tổng lợi nhuận gộp từ v_phantich_sanpham
        String sqlLN = "SELECT COALESCE(SUM(DoanhThu), 0) AS TongDT2, COALESCE(SUM(LoiNhuanGop), 0) AS TongLN " +
                       "FROM v_phantich_sanpham WHERE SoLuongThuc > 0";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sqlLN);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                kpi.put("tongLoiNhuan", rs.getBigDecimal("TongLN"));
            }
        }
        // Số mặt hàng có tồn kho > 0
        String sqlSP = "SELECT COUNT(*) AS SoSP FROM SanPham WHERE TonKho > 0";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sqlSP);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) kpi.put("soSanPham", rs.getLong("SoSP"));
        }
        return kpi;
    }
}
