package com.nhom6.qlbh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TraHang {
    private String maTra;
    private String maHD;
    private String maKH;
    private String tenKH;
    private Integer maNV;
    private String tenNV;
    private LocalDateTime thoiGian;
    private BigDecimal tongTienHang = BigDecimal.ZERO;
    private BigDecimal canTraKhach  = BigDecimal.ZERO;
    private BigDecimal daTraKhach   = BigDecimal.ZERO;
    private String lyDo;
    private String trangThai; // DA_TRA | DA_HUY
    private List<ChiTietTH> chiTiet = new ArrayList<>();

    public String getMaTra()       { return maTra; }
    public void setMaTra(String v) { maTra = v; }
    public String getMaHD()        { return maHD; }
    public void setMaHD(String v)  { maHD = v; }
    public String getMaKH()        { return maKH; }
    public void setMaKH(String v)  { maKH = v; }
    public String getTenKH()       { return tenKH != null ? tenKH : "Khách lẻ"; }
    public void setTenKH(String v) { tenKH = v; }
    public Integer getMaNV()         { return maNV; }
    public void setMaNV(Integer v)   { maNV = v; }
    public String getTenNV()       { return tenNV != null ? tenNV : "—"; }
    public void setTenNV(String v) { tenNV = v; }
    public LocalDateTime getThoiGian()        { return thoiGian; }
    public void setThoiGian(LocalDateTime v)  { thoiGian = v; }
    public BigDecimal getTongTienHang()           { return tongTienHang; }
    public void setTongTienHang(BigDecimal v)     { tongTienHang = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getCanTraKhach()            { return canTraKhach; }
    public void setCanTraKhach(BigDecimal v)      { canTraKhach = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getDaTraKhach()             { return daTraKhach; }
    public void setDaTraKhach(BigDecimal v)       { daTraKhach = v != null ? v : BigDecimal.ZERO; }
    public String getLyDo()        { return lyDo != null ? lyDo : ""; }
    public void setLyDo(String v)  { lyDo = v; }
    public String getTrangThai()       { return trangThai; }
    public void setTrangThai(String v) { trangThai = v; }
    public List<ChiTietTH> getChiTiet()       { return chiTiet; }
    public void setChiTiet(List<ChiTietTH> v) { chiTiet = v; }
}
