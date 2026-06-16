package com.nhom6.qlbh.model;

import java.math.BigDecimal;

public class PhanTichSanPham {
    private String maSP;
    private String tenSP;
    private int soLuongBan;
    private int soLuongTra;
    private int soLuongThuc;
    private BigDecimal doanhThu;
    private BigDecimal loiNhuanGop;

    public PhanTichSanPham(String maSP, String tenSP,
                           int soLuongBan, int soLuongTra, int soLuongThuc,
                           BigDecimal doanhThu, BigDecimal loiNhuanGop) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.soLuongBan = soLuongBan;
        this.soLuongTra = soLuongTra;
        this.soLuongThuc = soLuongThuc;
        this.doanhThu = doanhThu != null ? doanhThu : BigDecimal.ZERO;
        this.loiNhuanGop = loiNhuanGop != null ? loiNhuanGop : BigDecimal.ZERO;
    }

    public String getMaSP()            { return maSP; }
    public String getTenSP()           { return tenSP; }
    public int getSoLuongBan()         { return soLuongBan; }
    public int getSoLuongTra()         { return soLuongTra; }
    public int getSoLuongThuc()        { return soLuongThuc; }
    public BigDecimal getDoanhThu()    { return doanhThu; }
    public BigDecimal getLoiNhuanGop() { return loiNhuanGop; }
}
