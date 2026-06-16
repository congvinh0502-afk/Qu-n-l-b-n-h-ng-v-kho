package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.GiaoDichKhoDAO;
import com.nhom6.qlbh.dao.SanPhamDAO;
import com.nhom6.qlbh.model.ChiTietGDK;
import com.nhom6.qlbh.model.GiaoDichKho;
import com.nhom6.qlbh.model.SanPham;

import java.sql.SQLException;
import java.util.List;

public class GiaoDichKhoService {

    private final GiaoDichKhoDAO dao    = new GiaoDichKhoDAO();
    private final SanPhamDAO     spDAO  = new SanPhamDAO();

    public List<GiaoDichKho> findAll(String loaiGD) throws SQLException {
        return dao.findAll(loaiGD);
    }

    public List<ChiTietGDK> findChiTiet(String maGD) throws SQLException {
        return dao.findChiTiet(maGD);
    }

    public String generateMaGD(String loaiGD) throws SQLException {
        return dao.nextMaGD(loaiGD);
    }

    public void luu(GiaoDichKho gdk) throws Exception {
        // Basic validation
        if (gdk.getMaGD() == null || gdk.getMaGD().isBlank())
            throw new Exception("Mã giao dịch không được để trống.");
        if (gdk.getLoaiGD() == null || gdk.getLoaiGD().isBlank())
            throw new Exception("Chưa chọn loại giao dịch.");
        if (gdk.getChiTiet() == null || gdk.getChiTiet().isEmpty())
            throw new Exception("Cần có ít nhất 1 sản phẩm trong giao dịch.");
        if (dao.existsById(gdk.getMaGD()))
            throw new Exception("Mã \"" + gdk.getMaGD() + "\" đã tồn tại. Vui lòng tạo lại.");

        boolean needNCC = "NHAP".equals(gdk.getLoaiGD()) || "TRA_NHAP".equals(gdk.getLoaiGD());
        if (needNCC && (gdk.getMaNCC() == null || gdk.getMaNCC().isBlank()))
            throw new Exception("Loại giao dịch này cần chọn Nhà cung cấp.");

        // Stock validation for each line
        for (ChiTietGDK ct : gdk.getChiTiet()) {
            if (ct.getSoLuong() == 0)
                throw new Exception("Sản phẩm \"" + ct.getMaSP() + "\": Số lượng không được bằng 0.");

            SanPham sp = spDAO.findById(ct.getMaSP());
            if (sp == null)
                throw new Exception("Sản phẩm \"" + ct.getMaSP() + "\" không tồn tại.");

            String loai = gdk.getLoaiGD();
            if ("TRA_NHAP".equals(loai) || "XUAT_HUY".equals(loai)) {
                if (ct.getSoLuong() < 0)
                    throw new Exception("SP \"" + ct.getMaSP() + "\": Số lượng phải dương.");
                if (sp.getTonKho() < ct.getSoLuong())
                    throw new Exception("SP \"" + ct.getMaSP() + "\": Tồn kho chỉ còn " + sp.getTonKho() + ".");
            }
            if ("KIEM_KHO".equals(loai)) {
                if (sp.getTonKho() + ct.getSoLuong() < 0)
                    throw new Exception("SP \"" + ct.getMaSP() + "\": Điều chỉnh khiến tồn kho âm (hiện: " + sp.getTonKho() + ").");
            }
            if ("NHAP".equals(loai) && ct.getSoLuong() <= 0)
                throw new Exception("SP \"" + ct.getMaSP() + "\": Số lượng nhập phải lớn hơn 0.");
        }

        dao.insert(gdk);
    }
}
