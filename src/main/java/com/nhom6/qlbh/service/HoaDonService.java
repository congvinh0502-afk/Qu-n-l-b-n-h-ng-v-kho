package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.HoaDonDAO;
import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.HoaDon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class HoaDonService {

    private final HoaDonDAO dao = new HoaDonDAO();

    public List<HoaDon> findAll(String trangThai) throws SQLException { return dao.findAll(trangThai); }
    public HoaDon findById(String maHD) throws SQLException           { return dao.findById(maHD); }
    public List<ChiTietHD> findChiTiet(String maHD) throws SQLException { return dao.findChiTiet(maHD); }
    public String generateMaHD() throws SQLException                  { return dao.nextMaHD(); }

    /** Tạo hóa đơn không thanh toán ngay */
    public void tao(HoaDon hd) throws Exception {
        tao(hd, BigDecimal.ZERO, null);
    }

    /**
     * Tạo hóa đơn + tùy chọn thanh toán ngay.
     * soTienTrNgay = 0 hoặc null → không ghi ThanhToan, HĐ ở trạng thái CHUA_TT.
     * soTienTrNgay > 0 → ghi ThanhToan trong cùng transaction; trigger tự cập nhật trạng thái HĐ.
     */
    public void tao(HoaDon hd, BigDecimal soTienTrNgay, String hinhThuc) throws Exception {
        if (hd.getChiTiet() == null || hd.getChiTiet().isEmpty())
            throw new Exception("Cần ít nhất 1 sản phẩm trong hóa đơn.");

        BigDecimal tong = hd.getChiTiet().stream()
            .map(ChiTietHD::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        hd.setTongTienHang(tong);
        if (hd.getGiamGia() == null) hd.setGiamGia(BigDecimal.ZERO);
        if (hd.getGiamGia().compareTo(tong) > 0)
            throw new Exception("Giảm giá không được lớn hơn tổng tiền hàng.");
        hd.setTongSauGiamGia(tong.subtract(hd.getGiamGia()));

        if (soTienTrNgay != null && soTienTrNgay.compareTo(BigDecimal.ZERO) > 0) {
            if (soTienTrNgay.compareTo(hd.getTongSauGiamGia()) > 0)
                throw new Exception("Tiền thanh toán ngay vượt quá tổng sau giảm.");
            if (hinhThuc == null || hinhThuc.isBlank())
                throw new Exception("Chưa chọn hình thức thanh toán.");
        }

        try {
            dao.insert(hd, soTienTrNgay, hinhThuc);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Khong du ton kho") || msg.contains("ton kho")))
                throw new Exception("Không đủ tồn kho để bán một hoặc nhiều sản phẩm.");
            if (msg != null && msg.contains("San pham khong ton tai"))
                throw new Exception("Một hoặc nhiều sản phẩm không tồn tại.");
            throw e;
        }
    }
}
