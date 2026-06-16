package com.nhom6.qlbh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiaoDichKho {
    private String maGD;
    private String loaiGD;       // NHAP | TRA_NHAP | KIEM_KHO | XUAT_HUY
    private String maNCC;
    private String tenNCC;       // JOIN
    private Integer maNV;
    private String tenNV;        // JOIN
    private LocalDateTime thoiGian;
    private String ghiChu;
    private String trangThai;
    // aggregates for list display
    private int soMatHang;
    private BigDecimal tongTien = BigDecimal.ZERO;
    // detail lines (used when creating)
    private List<ChiTietGDK> chiTiet = new ArrayList<>();

    public GiaoDichKho() {}

    public String getMaGD()         { return maGD; }
    public void setMaGD(String v)   { maGD = v; }
    public String getLoaiGD()       { return loaiGD; }
    public void setLoaiGD(String v) { loaiGD = v; }
    public String getMaNCC()        { return maNCC; }
    public void setMaNCC(String v)  { maNCC = v; }
    public String getTenNCC()       { return tenNCC != null ? tenNCC : "—"; }
    public void setTenNCC(String v) { tenNCC = v; }
    public Integer getMaNV()         { return maNV; }
    public void setMaNV(Integer v)   { maNV = v; }
    public String getTenNV()        { return tenNV != null ? tenNV : "—"; }
    public void setTenNV(String v)  { tenNV = v; }
    public LocalDateTime getThoiGian()       { return thoiGian; }
    public void setThoiGian(LocalDateTime v) { thoiGian = v; }
    public String getGhiChu()       { return ghiChu != null ? ghiChu : ""; }
    public void setGhiChu(String v) { ghiChu = v; }
    public String getTrangThai()        { return trangThai; }
    public void setTrangThai(String v)  { trangThai = v; }
    public int getSoMatHang()        { return soMatHang; }
    public void setSoMatHang(int v)  { soMatHang = v; }
    public BigDecimal getTongTien()        { return tongTien; }
    public void setTongTien(BigDecimal v)  { tongTien = v != null ? v : BigDecimal.ZERO; }
    public List<ChiTietGDK> getChiTiet()   { return chiTiet; }
    public void setChiTiet(List<ChiTietGDK> v) { chiTiet = v; }

    public String getLoaiGDLabel() {
        if (loaiGD == null) return "";
        switch (loaiGD) {
            case "NHAP":     return "Nhập hàng";
            case "TRA_NHAP": return "Trả hàng nhập";
            case "KIEM_KHO": return "Kiểm kho";
            case "XUAT_HUY": return "Xuất hủy";
            default: return loaiGD;
        }
    }
}
