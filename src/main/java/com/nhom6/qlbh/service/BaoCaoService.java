package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.BaoCaoDAO;
import com.nhom6.qlbh.model.DoanhThuNgay;
import com.nhom6.qlbh.model.PhanTichSanPham;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BaoCaoService {

    private final BaoCaoDAO dao = new BaoCaoDAO();

    public List<DoanhThuNgay> getDoanhThuNgay(LocalDate from, LocalDate to) {
        try { return dao.getDoanhThuNgay(from, to); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải doanh thu: " + e.getMessage(), e); }
    }

    public List<PhanTichSanPham> getTopSanPham(int limit, String orderBy) {
        try { return dao.getTopSanPham(limit, orderBy); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải top SP: " + e.getMessage(), e); }
    }

    public List<PhanTichSanPham> getAllPhanTich() {
        try { return dao.getAllPhanTich(); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải phân tích: " + e.getMessage(), e); }
    }

    public Map<String, Object> getKpiTongQuan() {
        try { return dao.getKpiTongQuan(); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải KPI: " + e.getMessage(), e); }
    }
}
