package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class ChiTietTH {
    private String maTra;
    private String maSP;
    private String tenSP;
    private int soLuong;
    private BigDecimal donGia = BigDecimal.ZERO;
    // For the return form: original qty from invoice (not stored in DB)
    private int soLuongGoc;

    public ChiTietTH() {}
    public ChiTietTH(String maSP, String tenSP, int soLuong, BigDecimal donGia) {
        this.maSP = maSP; this.tenSP = tenSP; this.soLuong = soLuong; this.donGia = donGia;
    }

    public String getMaTra()         { return maTra; }
    public void setMaTra(String v)   { maTra = v; }
    public String getMaSP()          { return maSP; }
    public void setMaSP(String v)    { maSP = v; }
    public String getTenSP()         { return tenSP != null ? tenSP : ""; }
    public void setTenSP(String v)   { tenSP = v; }
    public int getSoLuong()          { return soLuong; }
    public void setSoLuong(int v)    { soLuong = v; }
    public BigDecimal getDonGia()    { return donGia != null ? donGia : BigDecimal.ZERO; }
    public void setDonGia(BigDecimal v) { donGia = v; }
    public int getSoLuongGoc()       { return soLuongGoc; }
    public void setSoLuongGoc(int v) { soLuongGoc = v; }

    public BigDecimal getThanhTien() { return getDonGia().multiply(BigDecimal.valueOf(soLuong)); }
}
