package com.nhom6.qlbh.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DoanhThuNgay {
    private LocalDate ngay;
    private BigDecimal doanhThuThuan;
    private int soHoaDon;

    public DoanhThuNgay(LocalDate ngay, BigDecimal doanhThuThuan, int soHoaDon) {
        this.ngay = ngay;
        this.doanhThuThuan = doanhThuThuan;
        this.soHoaDon = soHoaDon;
    }

    public LocalDate getNgay()           { return ngay; }
    public BigDecimal getDoanhThuThuan() { return doanhThuThuan != null ? doanhThuThuan : BigDecimal.ZERO; }
    public int getSoHoaDon()             { return soHoaDon; }
}
