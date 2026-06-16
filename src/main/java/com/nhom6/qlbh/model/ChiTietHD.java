package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class ChiTietHD {
    private String maHD;
    private String maSP;
    private String tenSP;
    private int soLuong;
    private BigDecimal donGia  = BigDecimal.ZERO;
    private BigDecimal giaVon  = BigDecimal.ZERO;

    public ChiTietHD() {}
    public ChiTietHD(String maSP, String tenSP, int soLuong, BigDecimal donGia, BigDecimal giaVon) {
        this.maSP = maSP; this.tenSP = tenSP;
        this.soLuong = soLuong; this.donGia = donGia; this.giaVon = giaVon;
    }

    public String getMaHD()          { return maHD; }
    public void setMaHD(String v)    { maHD = v; }
    public String getMaSP()          { return maSP; }
    public void setMaSP(String v)    { maSP = v; }
    public String getTenSP()         { return tenSP != null ? tenSP : ""; }
    public void setTenSP(String v)   { tenSP = v; }
    public int getSoLuong()          { return soLuong; }
    public void setSoLuong(int v)    { soLuong = v; }
    public BigDecimal getDonGia()    { return donGia != null ? donGia : BigDecimal.ZERO; }
    public void setDonGia(BigDecimal v)  { donGia = v; }
    public BigDecimal getGiaVon()    { return giaVon != null ? giaVon : BigDecimal.ZERO; }
    public void setGiaVon(BigDecimal v)  { giaVon = v; }

    public BigDecimal getThanhTien() { return getDonGia().multiply(BigDecimal.valueOf(soLuong)); }
}
