package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BanOnlineDAO {

    public List<NenTangOnline> getNenTangOnlines() throws SQLException {
        List<NenTangOnline> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("SELECT MaNT, TenNT FROM NenTangOnline ORDER BY MaNT");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new NenTangOnline(rs.getInt("MaNT"), rs.getString("TenNT")));
        }
        return list;
    }

    public List<DonHangOnline> findAll() throws SQLException {
        String sql = "SELECT d.MaDHO, d.MaNT, n.TenNT, d.MaHD, d.ThoiGian, d.TrangThai," +
                     "       v.MaVD, v.DiaChiGiao, v.DonViVanChuyen, v.TrangThaiGiao " +
                     "FROM DonHangOnline d " +
                     "JOIN NenTangOnline n ON n.MaNT = d.MaNT " +
                     "LEFT JOIN VanDon v ON v.MaHD = d.MaHD " +
                     "ORDER BY d.ThoiGian DESC";
        List<DonHangOnline> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DonHangOnline d = new DonHangOnline();
                d.setMaDHO(rs.getString("MaDHO"));
                d.setMaNT(rs.getInt("MaNT"));
                d.setTenNT(rs.getString("TenNT"));
                d.setMaHD(rs.getString("MaHD"));
                Timestamp ts = rs.getTimestamp("ThoiGian");
                if (ts != null) d.setThoiGian(ts.toLocalDateTime());
                d.setTrangThai(rs.getString("TrangThai"));
                d.setMaVD(rs.getString("MaVD"));
                d.setDiaChiGiao(rs.getString("DiaChiGiao"));
                d.setDonViVanChuyen(rs.getString("DonViVanChuyen"));
                d.setTrangThaiGiao(rs.getString("TrangThaiGiao"));
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Tạo toàn bộ đơn hàng online trong một transaction:
     * HoaDon → ChiTietHoaDon (trigger trừ kho) → DonHangOnline → VanDon
     */
    public void insertDonHangOnline(DonHangOnline don, HoaDon hoaDon,
                                    List<ChiTietHD> chiTiet,
                                    String diaChiGiao, String donVi) throws SQLException {
        try (Connection conn = DBConnection.get()) {
            conn.setAutoCommit(false);
            try {
                // 1. INSERT HoaDon
                String sqlHD = "INSERT INTO HoaDon(MaHD,ThoiGian,MaKH,MaNV,TongTienHang,GiamGia," +
                               "TongSauGiamGia,TrangThai,DaThanhToan) VALUES(?,NOW(),?,?,?,?,?,'CHUA_TT',0)";
                try (PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                    ps.setString(1, hoaDon.getMaHD());
                    ps.setString(2, hoaDon.getMaKH());
                    setNullableInt(ps, 3, hoaDon.getMaNV());
                    ps.setBigDecimal(4, hoaDon.getTongTienHang());
                    ps.setBigDecimal(5, hoaDon.getGiamGia());
                    ps.setBigDecimal(6, hoaDon.getTongSauGiamGia());
                    ps.executeUpdate();
                }

                // 2. INSERT ChiTietHoaDon (trigger trg_check_tonkho + trg_banhang_trutonkho fire here)
                String sqlCT = "INSERT INTO ChiTietHoaDon(MaHD,MaSP,SoLuong,DonGia,GiaVon) VALUES(?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                    for (ChiTietHD ct : chiTiet) {
                        ps.setString(1, hoaDon.getMaHD());
                        ps.setString(2, ct.getMaSP());
                        ps.setInt(3, ct.getSoLuong());
                        ps.setBigDecimal(4, ct.getDonGia());
                        ps.setBigDecimal(5, ct.getGiaVon());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 3. INSERT DonHangOnline
                String sqlDHO = "INSERT INTO DonHangOnline(MaDHO,MaNT,MaHD,ThoiGian,TrangThai) VALUES(?,?,?,NOW(),'DA_XU_LY')";
                try (PreparedStatement ps = conn.prepareStatement(sqlDHO)) {
                    ps.setString(1, don.getMaDHO());
                    ps.setInt(2, don.getMaNT());
                    ps.setString(3, hoaDon.getMaHD());
                    ps.executeUpdate();
                }

                // 4. INSERT VanDon
                String maVD = nextMaVD(conn);
                String sqlVD = "INSERT INTO VanDon(MaVD,MaHD,DiaChiGiao,DonViVanChuyen,TrangThaiGiao) VALUES(?,?,?,?,'CHO_LAY')";
                try (PreparedStatement ps = conn.prepareStatement(sqlVD)) {
                    ps.setString(1, maVD);
                    ps.setString(2, hoaDon.getMaHD());
                    ps.setString(3, diaChiGiao);
                    ps.setString(4, donVi);
                    ps.executeUpdate();
                }
                don.setMaVD(maVD);
                don.setMaHD(hoaDon.getMaHD());

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }

    /** Cập nhật trạng thái giao hàng vận đơn */
    public void updateTrangThaiGiao(String maVD, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("UPDATE VanDon SET TrangThaiGiao=? WHERE MaVD=?")) {
            ps.setString(1, newStatus);
            ps.setString(2, maVD);
            ps.executeUpdate();
        }
    }

    public String nextMaDHO(int maNT) throws SQLException {
        // Prefix by platform: 1=SHO, 2=LAZ, 3=TIK, 4=TTK, else DHO
        String prefix = switch (maNT) {
            case 1  -> "SHO";
            case 2  -> "LAZ";
            case 3  -> "TIK";
            case 4  -> "TTK";
            default -> "DHO";
        };
        String sql = "SELECT MAX(CAST(SUBSTRING(MaDHO," + (prefix.length()+1) + ") AS UNSIGNED)) FROM DonHangOnline WHERE MaDHO LIKE ?";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            long next = rs.next() ? rs.getLong(1) + 1 : 1;
            return prefix + String.format("%06d", next);
        }
    }

    private String nextMaVD(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(CAST(SUBSTRING(MaVD,3) AS UNSIGNED)) FROM VanDon WHERE MaVD LIKE 'VD%'");
             ResultSet rs = ps.executeQuery()) {
            long next = rs.next() ? rs.getLong(1) + 1 : 1;
            return "VD" + String.format("%06d", next);
        }
    }
}
