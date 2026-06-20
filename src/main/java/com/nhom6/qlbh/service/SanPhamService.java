package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.SanPhamDAO;
import com.nhom6.qlbh.model.SanPham;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SanPhamService {

    private final SanPhamDAO dao = new SanPhamDAO();

    public List<SanPham> findAll() throws SQLException {
        return dao.findAll();
    }

    public List<SanPham> search(String keyword, Integer maLoai) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            if (maLoai == null) return dao.findAll();
            return dao.search("", maLoai);
        }
        return dao.search(keyword, maLoai);
    }

    public void them(SanPham sp) throws Exception {
        validate(sp);
        if (dao.existsByMa(sp.getMaSP()))
            throw new Exception("Mã sản phẩm \"" + sp.getMaSP() + "\" đã tồn tại.");
        dao.insert(sp);
    }

    public void sua(SanPham sp) throws Exception {
        validate(sp);
        dao.update(sp);
    }

    public void xoa(String maSP) throws Exception {
        try {
            dao.delete(maSP);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("foreign key"))
                throw new Exception("Không thể xóa: sản phẩm đã có trong hóa đơn hoặc giao dịch kho.");
            throw e;
        }
    }

    public void updateMucTon(String maSP, int mucTon) throws Exception {
        dao.updateMucTon(maSP, mucTon);
    }

    public List<SanPham> findSapHet() throws Exception {
        return dao.findSapHet();
    }

    public int countSapHet() {
        try { return dao.countSapHet(); }
        catch (Exception e) { return 0; }
    }

    private void validate(SanPham sp) throws Exception {
        if (sp.getMaSP() == null || sp.getMaSP().isBlank())
            throw new Exception("Mã sản phẩm không được để trống.");
        if (sp.getTenSP() == null || sp.getTenSP().isBlank())
            throw new Exception("Tên sản phẩm không được để trống.");
        if (sp.getGiaBan() == null || sp.getGiaBan().compareTo(BigDecimal.ZERO) < 0)
            throw new Exception("Giá bán phải >= 0.");
        if (sp.getGiaVon() == null || sp.getGiaVon().compareTo(BigDecimal.ZERO) < 0)
            throw new Exception("Giá vốn phải >= 0.");
    }
}
