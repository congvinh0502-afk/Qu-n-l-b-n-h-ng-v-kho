package com.nhom6.qlbh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ThanhToan {
    private int maTT;
    private String maHD;
    private BigDecimal soTien = BigDecimal.ZERO;
    private String hinhThuc;  // TIEN_MAT | CHUYEN_KHOAN | THE
    private LocalDateTime thoiGian;

    public int getMaTT()         { return maTT; }
    public void setMaTT(int v)   { maTT = v; }
    public String getMaHD()      { return maHD; }
    public void setMaHD(String v){ maHD = v; }
    public BigDecimal getSoTien()        { return soTien; }
    public void setSoTien(BigDecimal v)  { soTien = v; }
    public String getHinhThuc()      { return hinhThuc; }
    public void setHinhThuc(String v){ hinhThuc = v; }
    public LocalDateTime getThoiGian()       { return thoiGian; }
    public void setThoiGian(LocalDateTime v) { thoiGian = v; }

    public String getHinhThucLabel() {
        if (hinhThuc == null) return "";
        switch (hinhThuc) {
            case "TIEN_MAT":     return "Tiền mặt";
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THE":          return "Thẻ";
            default:             return hinhThuc;
        }
    }
}
