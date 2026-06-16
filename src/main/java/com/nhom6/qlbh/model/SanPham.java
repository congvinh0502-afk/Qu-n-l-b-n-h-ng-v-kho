package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class SanPham {
    private String maSP;
    private String tenSP;
    private Integer maLoai;
    private String tenLoai;   // từ JOIN, chỉ dùng để hiển thị
    private BigDecimal giaVon;
    private BigDecimal giaBan;
    private int tonKho;
    private int trangThai;    // 1 = Đang KD, 0 = Ngừng KD

    public SanPham() {}

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public Integer getMaLoai() { return maLoai; }
    public void setMaLoai(Integer maLoai) { this.maLoai = maLoai; }
    public String getTenLoai() { return tenLoai != null ? tenLoai : "—"; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public BigDecimal getGiaVon() { return giaVon; }
    public void setGiaVon(BigDecimal giaVon) { this.giaVon = giaVon; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }
    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public String getTrangThaiLabel() { return trangThai == 1 ? "Đang KD" : "Ngừng KD"; }
}
