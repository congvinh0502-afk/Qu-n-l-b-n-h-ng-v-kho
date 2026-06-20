package com.nhom6.qlbh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHD;
    private LocalDateTime thoiGian;
    private String maKH;
    private String tenKH;
    private Integer maNV;
    private String tenNV;
    private BigDecimal tongTienHang    = BigDecimal.ZERO;
    private BigDecimal giamGia         = BigDecimal.ZERO;
    private BigDecimal tongSauGiamGia  = BigDecimal.ZERO;
    private BigDecimal daThanhToan     = BigDecimal.ZERO;
    private BigDecimal conNo           = BigDecimal.ZERO;
    private String trangThai; // DA_TT | MOT_PHAN | CHUA_TT
    private List<ChiTietHD> chiTiet    = new ArrayList<>();

    public String getMaHD()           { return maHD; }
    public void setMaHD(String v)     { maHD = v; }
    public LocalDateTime getThoiGian()       { return thoiGian; }
    public void setThoiGian(LocalDateTime v) { thoiGian = v; }
    public String getMaKH()           { return maKH; }
    public void setMaKH(String v)     { maKH = v; }
    public String getTenKH()          { return tenKH != null ? tenKH : "Khách lẻ"; }
    public void setTenKH(String v)    { tenKH = v; }
    public Integer getMaNV()          { return maNV; }
    public void setMaNV(Integer v)    { maNV = v; }
    public String getTenNV()          { return tenNV != null ? tenNV : "—"; }
    public void setTenNV(String v)    { tenNV = v; }
    public BigDecimal getTongTienHang()           { return tongTienHang; }
    public void setTongTienHang(BigDecimal v)     { tongTienHang = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getGiamGia()                { return giamGia; }
    public void setGiamGia(BigDecimal v)          { giamGia = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getTongSauGiamGia()         { return tongSauGiamGia; }
    public void setTongSauGiamGia(BigDecimal v)   { tongSauGiamGia = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getDaThanhToan()            { return daThanhToan; }
    public void setDaThanhToan(BigDecimal v)      { daThanhToan = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getConNo()                  { return conNo; }
    public void setConNo(BigDecimal v)            { conNo = v != null ? v : BigDecimal.ZERO; }
    public String getTrangThai()      { return trangThai; }
    public void setTrangThai(String v){ trangThai = v; }
    public List<ChiTietHD> getChiTiet()       { return chiTiet; }
    public void setChiTiet(List<ChiTietHD> v) { chiTiet = v; }

    /** Còn nợ — do DB trigger tính, đọc từ cột ConNo */
    public BigDecimal getConLai() { return conNo; }

    public String getTrangThaiLabel() {
        return switch (trangThai != null ? trangThai : "") {
            case "DA_TT"   -> "Đã TT";
            case "MOT_PHAN" -> "Trả một phần";
            default        -> "Chưa TT";
        };
    }
}
