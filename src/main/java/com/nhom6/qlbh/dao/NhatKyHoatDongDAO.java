package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.NhatKyHoatDong;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhatKyHoatDongDAO {

    public List<NhatKyHoatDong> findAll(LocalDate from, LocalDate to, String nguoiDung)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT MaLog, BangTacDong, HanhDong, MaBanGhi, MoTa, NguoiDung, ThoiGian " +
            "FROM NhatKyHoatDong WHERE 1=1");
        if (from != null)                              sql.append(" AND DATE(ThoiGian) >= ?");
        if (to != null)                                sql.append(" AND DATE(ThoiGian) <= ?");
        if (nguoiDung != null && !nguoiDung.isBlank()) sql.append(" AND NguoiDung = ?");
        sql.append(" ORDER BY ThoiGian DESC LIMIT 500");

        List<NhatKyHoatDong> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (from != null)                              ps.setDate(idx++, Date.valueOf(from));
            if (to != null)                                ps.setDate(idx++, Date.valueOf(to));
            if (nguoiDung != null && !nguoiDung.isBlank()) ps.setString(idx, nguoiDung);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhatKyHoatDong log = new NhatKyHoatDong();
                    log.setMaLog(rs.getLong("MaLog"));
                    log.setBangTacDong(rs.getString("BangTacDong"));
                    log.setHanhDong(rs.getString("HanhDong"));
                    log.setMaBanGhi(rs.getString("MaBanGhi"));
                    log.setMoTa(rs.getString("MoTa"));
                    log.setNguoiDung(rs.getString("NguoiDung"));
                    Timestamp ts = rs.getTimestamp("ThoiGian");
                    if (ts != null) log.setThoiGian(ts.toLocalDateTime());
                    list.add(log);
                }
            }
        }
        return list;
    }

    public List<String> getNguoiDungs() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT DISTINCT NguoiDung FROM NhatKyHoatDong ORDER BY NguoiDung");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }
}
