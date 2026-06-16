package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private String dienThoai;
    private BigDecimal tongBan    = BigDecimal.ZERO;
    private BigDecimal tongBanTruTra = BigDecimal.ZERO;

    public KhachHang() {}

    public String getMaKH()       { return maKH; }
    public void setMaKH(String v) { maKH = v; }
    public String getTenKH()       { return tenKH; }
    public void setTenKH(String v) { tenKH = v; }
    public String getDienThoai()       { return dienThoai != null ? dienThoai : ""; }
    public void setDienThoai(String v) { dienThoai = v; }
    public BigDecimal getTongBan()       { return tongBan; }
    public void setTongBan(BigDecimal v) { tongBan = v; }
    public BigDecimal getTongBanTruTra()       { return tongBanTruTra; }
    public void setTongBanTruTra(BigDecimal v) { tongBanTruTra = v; }
}
