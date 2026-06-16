package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.HoaDonDAO;
import com.nhom6.qlbh.dao.TraHangDAO;
import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.ChiTietTH;
import com.nhom6.qlbh.model.TraHang;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TraHangService {

    private final TraHangDAO  dao    = new TraHangDAO();
    private final HoaDonDAO   hdDAO  = new HoaDonDAO();

    public List<TraHang> findAll() throws SQLException { return dao.findAll(); }
    public List<ChiTietTH> findChiTiet(String maTra) throws SQLException { return dao.findChiTiet(maTra); }
    public String generateMaTra() throws SQLException { return dao.nextMaTra(); }
    public List<ChiTietHD> findChiTietHoaDon(String maHD) throws SQLException { return hdDAO.findChiTiet(maHD); }

    public void tao(TraHang th) throws Exception {
        if (th.getChiTiet() == null || th.getChiTiet().isEmpty())
            throw new Exception("Cần ít nhất 1 sản phẩm trả.");

        // Validate quantities against original invoice (if maHD provided)
        if (th.getMaHD() != null && !th.getMaHD().isBlank()) {
            List<ChiTietHD> goc = hdDAO.findChiTiet(th.getMaHD());
            for (ChiTietTH ct : th.getChiTiet()) {
                ChiTietHD orig = goc.stream()
                    .filter(g -> g.getMaSP().equals(ct.getMaSP())).findFirst().orElse(null);
                if (orig == null)
                    throw new Exception("SP \"" + ct.getMaSP() + "\" không có trong hóa đơn " + th.getMaHD() + ".");
                if (ct.getSoLuong() > orig.getSoLuong())
                    throw new Exception("SP \"" + ct.getMaSP() + "\": Số lượng trả (" + ct.getSoLuong() + ") vượt quá số đã mua (" + orig.getSoLuong() + ").");
            }
        }

        // Compute totals
        BigDecimal tong = th.getChiTiet().stream()
            .map(ChiTietTH::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        th.setTongTienHang(tong);
        th.setCanTraKhach(tong);
        if (th.getDaTraKhach() == null) th.setDaTraKhach(BigDecimal.ZERO);

        dao.insert(th);
    }
}
