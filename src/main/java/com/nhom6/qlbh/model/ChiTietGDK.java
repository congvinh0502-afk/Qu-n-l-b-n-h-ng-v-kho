package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class ChiTietGDK {
    private String maGD;
    private String maSP;
    private String tenSP;     // JOIN
    private int soLuong;
    private BigDecimal donGiaNhap = BigDecimal.ZERO;

    public ChiTietGDK() {}
    public ChiTietGDK(String maSP, String tenSP, int soLuong, BigDecimal donGiaNhap) {
        this.maSP = maSP; this.tenSP = tenSP;
        this.soLuong = soLuong; this.donGiaNhap = donGiaNhap;
    }

    public String getMaGD()          { return maGD; }
    public void setMaGD(String v)    { maGD = v; }
    public String getMaSP()          { return maSP; }
    public void setMaSP(String v)    { maSP = v; }
    public String getTenSP()         { return tenSP != null ? tenSP : ""; }
    public void setTenSP(String v)   { tenSP = v; }
    public int getSoLuong()          { return soLuong; }
    public void setSoLuong(int v)    { soLuong = v; }
    public BigDecimal getDonGiaNhap()        { return donGiaNhap != null ? donGiaNhap : BigDecimal.ZERO; }
    public void setDonGiaNhap(BigDecimal v)  { donGiaNhap = v; }

    public BigDecimal getThanhTien() {
        return getDonGiaNhap().multiply(BigDecimal.valueOf(soLuong));
    }
}
