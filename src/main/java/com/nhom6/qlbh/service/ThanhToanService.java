package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.ThanhToanDAO;
import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.model.ThanhToan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ThanhToanService {

    private final ThanhToanDAO dao = new ThanhToanDAO();

    public List<ThanhToan> findByHoaDon(String maHD) throws SQLException {
        return dao.findByHoaDon(maHD);
    }

    public void thanhToan(HoaDon hd, BigDecimal soTien, String hinhThuc) throws Exception {
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0)
            throw new Exception("Số tiền phải lớn hơn 0.");
        if ("DA_TT".equals(hd.getTrangThai()))
            throw new Exception("Hóa đơn này đã được thanh toán đầy đủ.");
        BigDecimal conLai = hd.getConLai();
        if (soTien.compareTo(conLai) > 0)
            throw new Exception("Số tiền vượt quá số còn lại (" + conLai + " đ). Điều chỉnh lại.");
        if (hinhThuc == null || hinhThuc.isBlank())
            throw new Exception("Chưa chọn hình thức thanh toán.");
        dao.insert(hd.getMaHD(), soTien, hinhThuc);
    }
}
