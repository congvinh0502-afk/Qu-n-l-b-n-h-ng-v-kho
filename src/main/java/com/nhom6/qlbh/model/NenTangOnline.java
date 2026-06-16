package com.nhom6.qlbh.model;

public class NenTangOnline {
    private int maNT;
    private String tenNT;

    public NenTangOnline() {}
    public NenTangOnline(int maNT, String tenNT) { this.maNT = maNT; this.tenNT = tenNT; }

    public int getMaNT()     { return maNT; }
    public String getTenNT() { return tenNT; }
    @Override public String toString() { return tenNT; }
}
