package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.BanOnlineDAO;
import com.nhom6.qlbh.dao.HoaDonDAO;
import com.nhom6.qlbh.model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BanOnlineService {

    private final BanOnlineDAO dao    = new BanOnlineDAO();
    private final HoaDonDAO    hdDAO  = new HoaDonDAO();

    public List<NenTangOnline> getNenTangOnlines() {
        try { return dao.getNenTangOnlines(); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải nền tảng: " + e.getMessage(), e); }
    }

    public List<DonHangOnline> findAll() {
        try { return dao.findAll(); }
        catch (Exception e) { throw new RuntimeException("Lỗi tải đơn online: " + e.getMessage(), e); }
    }

    public String generateMaDHO(int maNT) {
        try { return dao.nextMaDHO(maNT); }
        catch (Exception e) { throw new RuntimeException("Lỗi sinh mã: " + e.getMessage(), e); }
    }

    public String generateMaHD() {
        try { return hdDAO.nextMaHD(); }
        catch (Exception e) { throw new RuntimeException("Lỗi sinh MaHD: " + e.getMessage(), e); }
    }

    /** Validate và tạo đơn hàng online + hóa đơn + vận đơn trong một transaction */
    public void taoDoHang(DonHangOnline don, HoaDon hoaDon,
                          List<ChiTietHD> chiTiet, String diaChiGiao, String donVi) {
        if (chiTiet == null || chiTiet.isEmpty())
            throw new IllegalArgumentException("Chưa có sản phẩm nào trong đơn hàng.");
        if (diaChiGiao == null || diaChiGiao.isBlank())
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng.");

        // Tính tổng
        BigDecimal tongTienHang = chiTiet.stream()
            .map(ChiTietHD::getThanhTien)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giam = hoaDon.getGiamGia() != null ? hoaDon.getGiamGia() : BigDecimal.ZERO;
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setGiamGia(giam);
        hoaDon.setTongSauGiamGia(tongTienHang.subtract(giam.min(tongTienHang)));

        try {
            dao.insertDonHangOnline(don, hoaDon, chiTiet, diaChiGiao, donVi);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Khong du ton kho"))
                throw new RuntimeException("Không đủ tồn kho để bán!", e);
            throw new RuntimeException("Lỗi tạo đơn hàng online: " + msg, e);
        }
    }

    public void capNhatVanChuyen(String maVD, String newStatus) {
        try { dao.updateTrangThaiGiao(maVD, newStatus); }
        catch (Exception e) { throw new RuntimeException("Lỗi cập nhật vận chuyển: " + e.getMessage(), e); }
    }
}
