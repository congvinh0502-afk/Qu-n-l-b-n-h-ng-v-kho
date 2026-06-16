package com.nhom6.qlbh.model;

public class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String matKhau;
    private VaiTro vaiTro;
    private NhanVien nhanVien;

    public TaiKhoan() {}

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public String getTenNV() {
        return nhanVien != null ? nhanVien.getTenNV() : tenDangNhap;
    }
}
