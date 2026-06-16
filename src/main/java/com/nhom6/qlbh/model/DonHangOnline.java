package com.nhom6.qlbh.model;

import java.time.LocalDateTime;

/** Đơn hàng online — bao gồm thông tin JOIN từ NenTangOnline và VanDon để hiển thị danh sách */
public class DonHangOnline {
    private String maDHO;
    private int    maNT;
    private String tenNT;       // JOIN NenTangOnline
    private String maHD;
    private LocalDateTime thoiGian;
    private String trangThai;

    // JOIN VanDon
    private String maVD;
    private String diaChiGiao;
    private String donViVanChuyen;
    private String trangThaiGiao;

    public String getMaDHO()          { return maDHO; }
    public void setMaDHO(String v)    { maDHO = v; }
    public int getMaNT()              { return maNT; }
    public void setMaNT(int v)        { maNT = v; }
    public String getTenNT()          { return tenNT; }
    public void setTenNT(String v)    { tenNT = v; }
    public String getMaHD()           { return maHD; }
    public void setMaHD(String v)     { maHD = v; }
    public LocalDateTime getThoiGian()    { return thoiGian; }
    public void setThoiGian(LocalDateTime v) { thoiGian = v; }
    public String getTrangThai()          { return trangThai; }
    public void setTrangThai(String v)    { trangThai = v; }
    public String getMaVD()               { return maVD; }
    public void setMaVD(String v)         { maVD = v; }
    public String getDiaChiGiao()         { return diaChiGiao; }
    public void setDiaChiGiao(String v)   { diaChiGiao = v; }
    public String getDonViVanChuyen()     { return donViVanChuyen; }
    public void setDonViVanChuyen(String v) { donViVanChuyen = v; }
    public String getTrangThaiGiao()      { return trangThaiGiao; }
    public void setTrangThaiGiao(String v){ trangThaiGiao = v; }

    public String getTrangThaiGiaoLabel() {
        if (trangThaiGiao == null) return "—";
        return switch (trangThaiGiao) {
            case "CHO_LAY"    -> "Chờ lấy hàng";
            case "DANG_GIAO"  -> "Đang giao";
            case "DA_GIAO"    -> "Đã giao";
            case "HOAN"       -> "Hoàn trả";
            default           -> trangThaiGiao;
        };
    }
}
