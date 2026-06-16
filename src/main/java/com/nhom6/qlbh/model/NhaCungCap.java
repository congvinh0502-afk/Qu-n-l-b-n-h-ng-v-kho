package com.nhom6.qlbh.model;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String dienThoai;
    private String diaChi;

    public NhaCungCap() {}

    public String getMaNCC()       { return maNCC; }
    public void setMaNCC(String v) { maNCC = v; }
    public String getTenNCC()       { return tenNCC; }
    public void setTenNCC(String v) { tenNCC = v; }
    public String getDienThoai()       { return dienThoai != null ? dienThoai : ""; }
    public void setDienThoai(String v) { dienThoai = v; }
    public String getDiaChi()       { return diaChi != null ? diaChi : ""; }
    public void setDiaChi(String v) { diaChi = v; }
}
