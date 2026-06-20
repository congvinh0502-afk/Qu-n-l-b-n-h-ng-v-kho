package com.nhom6.qlbh.model;

public class NenTangOnline {
    private int maNT;
    private String tenNT;
    private int trangThaiKetNoi; // 0 = chưa kết nối, 1 = đã kết nối
    private String mauSac;

    public NenTangOnline() {}
    public NenTangOnline(int maNT, String tenNT) { this.maNT = maNT; this.tenNT = tenNT; }

    public int getMaNT()               { return maNT; }
    public void setMaNT(int v)         { maNT = v; }
    public String getTenNT()           { return tenNT; }
    public void setTenNT(String v)     { tenNT = v; }
    public int getTrangThaiKetNoi()    { return trangThaiKetNoi; }
    public void setTrangThaiKetNoi(int v) { trangThaiKetNoi = v; }
    public String getMauSac()          { return mauSac; }
    public void setMauSac(String v)    { mauSac = v; }
    public boolean isKetNoi()          { return trangThaiKetNoi == 1; }

    @Override public String toString() { return tenNT; }
}
