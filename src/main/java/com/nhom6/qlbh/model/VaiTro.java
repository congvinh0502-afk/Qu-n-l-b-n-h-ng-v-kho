package com.nhom6.qlbh.model;

public enum VaiTro {
    QUANLY("Quản lý"),
    BANHANG("Bán hàng"),
    KHO("Kho"),
    CSKH("CSKH");

    private final String label;
    VaiTro(String label) { this.label = label; }
    public String getLabel() { return label; }
}
