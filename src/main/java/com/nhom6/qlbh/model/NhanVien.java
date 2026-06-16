package com.nhom6.qlbh.model;

public class NhanVien {
    private int maNV;
    private String tenNV;
    private String dienThoai;
    private String chucVu;

    public NhanVien() {}

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }
    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }
    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }
    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    @Override
    public String toString() { return tenNV; }
}
