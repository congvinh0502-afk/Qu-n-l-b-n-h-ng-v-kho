package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.KhachHangDAO;
import com.nhom6.qlbh.model.KhachHang;

import java.sql.SQLException;
import java.util.List;

public class KhachHangService {

    private final KhachHangDAO dao = new KhachHangDAO();

    public List<KhachHang> findAll() throws SQLException { return dao.findAll(); }

    public List<KhachHang> search(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return dao.findAll();
        return dao.search(keyword);
    }

    public void them(KhachHang kh) throws Exception {
        validate(kh);
        if (dao.existsByMa(kh.getMaKH()))
            throw new Exception("Mã khách hàng \"" + kh.getMaKH() + "\" đã tồn tại.");
        dao.insert(kh);
    }

    public void sua(KhachHang kh) throws Exception {
        validate(kh);
        dao.update(kh);
    }

    public void xoa(String maKH) throws Exception {
        try {
            dao.delete(maKH);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("foreign key"))
                throw new Exception("Không thể xóa: khách hàng đã có hóa đơn.");
            throw e;
        }
    }

    private void validate(KhachHang kh) throws Exception {
        if (kh.getMaKH() == null || kh.getMaKH().isBlank())
            throw new Exception("Mã khách hàng không được để trống.");
        if (kh.getTenKH() == null || kh.getTenKH().isBlank())
            throw new Exception("Tên khách hàng không được để trống.");
    }
}
