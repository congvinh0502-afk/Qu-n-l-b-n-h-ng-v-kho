package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.NhatKyHoatDongDAO;
import com.nhom6.qlbh.model.NhatKyHoatDong;

import java.time.LocalDate;
import java.util.List;

public class NhatKyHoatDongService {

    private final NhatKyHoatDongDAO dao = new NhatKyHoatDongDAO();

    public List<NhatKyHoatDong> findAll(LocalDate from, LocalDate to, String nguoiDung) {
        try { return dao.findAll(from, to, nguoiDung); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải nhật ký: " + e.getMessage(), e); }
    }

    public List<String> getNguoiDungs() {
        try { return dao.getNguoiDungs(); }
        catch (Exception e) { return List.of(); }
    }
}
