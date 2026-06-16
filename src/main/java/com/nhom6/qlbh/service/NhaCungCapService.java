package com.nhom6.qlbh.service;

import com.nhom6.qlbh.dao.NhaCungCapDAO;
import com.nhom6.qlbh.model.NhaCungCap;

import java.sql.SQLException;
import java.util.List;

public class NhaCungCapService {

    private final NhaCungCapDAO dao = new NhaCungCapDAO();

    public List<NhaCungCap> findAll() throws SQLException { return dao.findAll(); }

    public List<NhaCungCap> search(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return dao.findAll();
        return dao.search(keyword);
    }

    public void them(NhaCungCap ncc) throws Exception {
        validate(ncc);
        if (dao.existsByMa(ncc.getMaNCC()))
            throw new Exception("Mã nhà cung cấp \"" + ncc.getMaNCC() + "\" đã tồn tại.");
        dao.insert(ncc);
    }

    public void sua(NhaCungCap ncc) throws Exception {
        validate(ncc);
        dao.update(ncc);
    }

    public void xoa(String maNCC) throws Exception {
        try {
            dao.delete(maNCC);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("foreign key"))
                throw new Exception("Không thể xóa: nhà cung cấp đã có phiếu nhập hàng.");
            throw e;
        }
    }

    private void validate(NhaCungCap ncc) throws Exception {
        if (ncc.getMaNCC() == null || ncc.getMaNCC().isBlank())
            throw new Exception("Mã nhà cung cấp không được để trống.");
        if (ncc.getTenNCC() == null || ncc.getTenNCC().isBlank())
            throw new Exception("Tên nhà cung cấp không được để trống.");
    }
}
