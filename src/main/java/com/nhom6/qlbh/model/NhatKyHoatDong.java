package com.nhom6.qlbh.model;

import java.time.LocalDateTime;

public class NhatKyHoatDong {
    private long maLog;
    private String bangTacDong;
    private String hanhDong;
    private String maBanGhi;
    private String moTa;
    private String nguoiDung;
    private LocalDateTime thoiGian;

    public long getMaLog()              { return maLog; }
    public void setMaLog(long v)        { maLog = v; }
    public String getBangTacDong()      { return bangTacDong; }
    public void setBangTacDong(String v){ bangTacDong = v; }
    public String getHanhDong()         { return hanhDong; }
    public void setHanhDong(String v)   { hanhDong = v; }
    public String getMaBanGhi()         { return maBanGhi; }
    public void setMaBanGhi(String v)   { maBanGhi = v; }
    public String getMoTa()             { return moTa; }
    public void setMoTa(String v)       { moTa = v; }
    public String getNguoiDung()        { return nguoiDung; }
    public void setNguoiDung(String v)  { nguoiDung = v; }
    public LocalDateTime getThoiGian()  { return thoiGian; }
    public void setThoiGian(LocalDateTime v){ thoiGian = v; }

    public String getHanhDongLabel() {
        if (hanhDong == null) return "—";
        return switch (hanhDong) {
            case "THEM" -> "Thêm";
            case "SUA"  -> "Sửa";
            case "XOA"  -> "Xóa";
            default     -> hanhDong;
        };
    }
}
