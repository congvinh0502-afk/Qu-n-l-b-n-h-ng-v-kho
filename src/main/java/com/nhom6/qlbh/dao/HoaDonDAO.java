package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.HoaDon;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private static final String BASE_SELECT =
        "SELECT hd.MaHD, hd.ThoiGian, hd.MaKH, kh.TenKH, hd.MaNV, nv.TenNV, " +
        "hd.TongTienHang, hd.GiamGia, hd.TongSauGiamGia, hd.DaThanhToan, hd.ConNo, hd.TrangThai " +
        "FROM HoaDon hd " +
        "LEFT JOIN KhachHang kh ON kh.MaKH = hd.MaKH " +
        "LEFT JOIN NhanVien  nv ON nv.MaNV = hd.MaNV ";

    public List<HoaDon> findAll(String trangThai) throws SQLException {
        String sql = BASE_SELECT + (trangThai != null ? "WHERE hd.TrangThai = ? " : "") + "ORDER BY hd.ThoiGian DESC";
        List<HoaDon> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (trangThai != null) ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public HoaDon findById(String maHD) throws SQLException {
        String sql = BASE_SELECT + "WHERE hd.MaHD = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<ChiTietHD> findChiTiet(String maHD) throws SQLException {
        String sql = "SELECT ct.MaHD, ct.MaSP, sp.TenSP, ct.SoLuong, ct.DonGia, ct.GiaVon " +
                     "FROM ChiTietHoaDon ct JOIN SanPham sp ON sp.MaSP = ct.MaSP WHERE ct.MaHD = ?";
        List<ChiTietHD> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHD ct = new ChiTietHD();
                    ct.setMaHD(rs.getString("MaHD"));
                    ct.setMaSP(rs.getString("MaSP"));
                    ct.setTenSP(rs.getString("TenSP"));
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGia(rs.getBigDecimal("DonGia"));
                    ct.setGiaVon(rs.getBigDecimal("GiaVon"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    public String nextMaHD() throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(MaHD, 3) AS UNSIGNED)), 0) + 1 FROM HoaDon WHERE MaHD LIKE 'HD%'";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return "HD" + String.format("%03d", rs.getInt(1));
        }
    }

    /** Tạo hóa đơn (không thanh toán ngay) */
    public void insert(HoaDon hd) throws SQLException {
        insert(hd, BigDecimal.ZERO, null);
    }

    /**
     * Tạo hóa đơn + tùy chọn thanh toán ngay trong cùng một transaction.
     * Trigger trg_hd_tinh_trangthai_ins tự tính ConNo + TrangThai từ DaThanhToan=0.
     * Sau đó nếu có soTienTrNgay > 0, INSERT ThanhToan → trigger trg_tt_dongbo_hoadon_ins
     * cập nhật lại DaThanhToan/ConNo/TrangThai của HoaDon tự động.
     */
    public void insert(HoaDon hd, BigDecimal soTienTrNgay, String hinhThuc) throws SQLException {
        String sqlH  = "INSERT INTO HoaDon (MaHD, ThoiGian, MaKH, MaNV, TongTienHang, GiamGia, TongSauGiamGia, DaThanhToan) " +
                       "VALUES (?, NOW(), ?, ?, ?, ?, ?, 0)";
        String sqlD  = "INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGia, GiaVon) VALUES (?, ?, ?, ?, ?)";
        String sqlTT = "INSERT INTO ThanhToan (MaHD, SoTien, HinhThuc, ThoiGian) VALUES (?, ?, ?, NOW())";

        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlH)) {
                    ps.setString(1, hd.getMaHD());
                    if (hd.getMaKH() != null && !hd.getMaKH().isBlank()) ps.setString(2, hd.getMaKH());
                    else ps.setNull(2, Types.VARCHAR);
                    if (hd.getMaNV() != null) ps.setInt(3, hd.getMaNV());
                    else ps.setNull(3, Types.INTEGER);
                    ps.setBigDecimal(4, hd.getTongTienHang());
                    ps.setBigDecimal(5, hd.getGiamGia());
                    ps.setBigDecimal(6, hd.getTongSauGiamGia());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(sqlD)) {
                    for (ChiTietHD ct : hd.getChiTiet()) {
                        ps.setString(1, hd.getMaHD()); ps.setString(2, ct.getMaSP());
                        ps.setInt(3, ct.getSoLuong()); ps.setBigDecimal(4, ct.getDonGia());
                        ps.setBigDecimal(5, ct.getGiaVon()); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                if (soTienTrNgay != null && soTienTrNgay.compareTo(BigDecimal.ZERO) > 0) {
                    try (PreparedStatement ps = c.prepareStatement(sqlTT)) {
                        ps.setString(1, hd.getMaHD());
                        ps.setBigDecimal(2, soTienTrNgay);
                        ps.setString(3, hinhThuc != null ? hinhThuc : "Tiền mặt");
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("MaHD"));
        Timestamp ts = rs.getTimestamp("ThoiGian");
        if (ts != null) hd.setThoiGian(ts.toLocalDateTime());
        hd.setMaKH(rs.getString("MaKH")); hd.setTenKH(rs.getString("TenKH"));
        int maNV = rs.getInt("MaNV"); hd.setMaNV(rs.wasNull() ? null : maNV);
        hd.setTenNV(rs.getString("TenNV"));
        hd.setTongTienHang(rs.getBigDecimal("TongTienHang"));
        hd.setGiamGia(rs.getBigDecimal("GiamGia"));
        hd.setTongSauGiamGia(rs.getBigDecimal("TongSauGiamGia"));
        hd.setDaThanhToan(rs.getBigDecimal("DaThanhToan"));
        hd.setConNo(rs.getBigDecimal("ConNo"));
        hd.setTrangThai(rs.getString("TrangThai"));
        return hd;
    }
}
