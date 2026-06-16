package com.nhom6.qlbh.dao;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.model.LoaiSanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiSanPhamDAO {

    public List<LoaiSanPham> findAll() throws SQLException {
        List<LoaiSanPham> list = new ArrayList<>();
        String sql = "SELECT MaLoai, TenLoai FROM LoaiSanPham ORDER BY TenLoai";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LoaiSanPham(rs.getInt("MaLoai"), rs.getString("TenLoai")));
            }
        }
        return list;
    }
}
