-- =====================================================================
-- 01_schema.sql
-- HỆ THỐNG QUẢN LÝ BÁN HÀNG & KHO - Nhóm 6
-- MySQL 8 / utf8mb4
-- Thứ tự CREATE TABLE đã sắp theo phụ thuộc khóa ngoại (bảng cha trước).
-- Đã PHÁ chu trình HoaDon <-> TraHang: chỉ giữ 1 chiều TraHang.MaHD -> HoaDon.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS qlbh
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE qlbh;

-- Xóa bảng cũ (chạy lại được nhiều lần) - xóa theo thứ tự ngược phụ thuộc
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS DonHangOnline;
DROP TABLE IF EXISTS NenTangOnline;
DROP TABLE IF EXISTS VanDon;
DROP TABLE IF EXISTS YeuCauSuaChua;
DROP TABLE IF EXISTS ChiTietGiaoDichKho;
DROP TABLE IF EXISTS GiaoDichKho;
DROP TABLE IF EXISTS ChiTietTraHang;
DROP TABLE IF EXISTS TraHang;
DROP TABLE IF EXISTS ThanhToan;
DROP TABLE IF EXISTS ChiTietHoaDon;
DROP TABLE IF EXISTS HoaDon;
DROP TABLE IF EXISTS ChiTietDonDatHang;
DROP TABLE IF EXISTS DonDatHang;
DROP TABLE IF EXISTS TaiKhoan;
DROP TABLE IF EXISTS NhanVien;
DROP TABLE IF EXISTS KhachHang;
DROP TABLE IF EXISTS SanPham;
DROP TABLE IF EXISTS NhaCungCap;
DROP TABLE IF EXISTS LoaiSanPham;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- NHÓM 1: DANH MỤC
-- =====================================================================

-- 1. Loại sản phẩm
CREATE TABLE LoaiSanPham (
    MaLoai   INT AUTO_INCREMENT PRIMARY KEY,
    TenLoai  VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- 2. Nhà cung cấp
CREATE TABLE NhaCungCap (
    MaNCC     VARCHAR(20) PRIMARY KEY,
    TenNCC    VARCHAR(150) NOT NULL,
    DienThoai VARCHAR(20),
    DiaChi    VARCHAR(255)
) ENGINE=InnoDB;

-- 3. Sản phẩm
CREATE TABLE SanPham (
    MaSP      VARCHAR(30) PRIMARY KEY,
    TenSP     VARCHAR(255) NOT NULL,
    MaLoai    INT,
    GiaVon    DECIMAL(15,2) NOT NULL DEFAULT 0,
    GiaBan    DECIMAL(15,2) NOT NULL DEFAULT 0,
    TonKho    INT NOT NULL DEFAULT 0,           -- cập nhật tự động bằng trigger
    TrangThai TINYINT NOT NULL DEFAULT 1,        -- 1=đang KD, 0=ngừng
    CONSTRAINT fk_sp_loai FOREIGN KEY (MaLoai)
        REFERENCES LoaiSanPham(MaLoai)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Khách hàng
CREATE TABLE KhachHang (
    MaKH          VARCHAR(20) PRIMARY KEY,
    TenKH         VARCHAR(150) NOT NULL,
    DienThoai     VARCHAR(30),
    TongBan       DECIMAL(18,2) NOT NULL DEFAULT 0,   -- tự động cập nhật
    TongBanTruTra DECIMAL(18,2) NOT NULL DEFAULT 0    -- tự động cập nhật
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 2: NGƯỜI DÙNG HỆ THỐNG
-- =====================================================================

-- 5. Nhân viên
CREATE TABLE NhanVien (
    MaNV      INT AUTO_INCREMENT PRIMARY KEY,
    TenNV     VARCHAR(150) NOT NULL,
    DienThoai VARCHAR(20),
    ChucVu    VARCHAR(50)
) ENGINE=InnoDB;

-- 6. Tài khoản
CREATE TABLE TaiKhoan (
    MaTK         INT AUTO_INCREMENT PRIMARY KEY,
    TenDangNhap  VARCHAR(50) NOT NULL UNIQUE,
    MatKhau      VARCHAR(255) NOT NULL,           -- lưu hash BCrypt
    MaNV         INT,
    VaiTro       ENUM('QUANLY','BANHANG','KHO','CSKH') NOT NULL DEFAULT 'BANHANG',
    CONSTRAINT fk_tk_nv FOREIGN KEY (MaNV)
        REFERENCES NhanVien(MaNV)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 3: ĐẶT HÀNG
-- =====================================================================

-- 7. Đơn đặt hàng
CREATE TABLE DonDatHang (
    MaDH           VARCHAR(20) PRIMARY KEY,
    ThoiGian       DATETIME NOT NULL,
    MaKH           VARCHAR(20),
    MaNV           INT,
    TongTienHang   DECIMAL(18,2) NOT NULL DEFAULT 0,
    GiamGia        DECIMAL(18,2) NOT NULL DEFAULT 0,
    TongSauGiamGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    TrangThai      ENUM('PHIEU_TAM','HOAN_THANH') NOT NULL DEFAULT 'PHIEU_TAM',
    CONSTRAINT fk_dh_kh FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_dh_nv FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. Chi tiết đơn đặt hàng (PK kép)
CREATE TABLE ChiTietDonDatHang (
    MaDH     VARCHAR(20) NOT NULL,
    MaSP     VARCHAR(30) NOT NULL,
    SoLuong  INT NOT NULL,
    DonGia   DECIMAL(15,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (MaDH, MaSP),
    CONSTRAINT fk_ctdh_dh FOREIGN KEY (MaDH) REFERENCES DonDatHang(MaDH)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ctdh_sp FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 4: HÓA ĐƠN & THANH TOÁN
-- =====================================================================

-- 9. Hóa đơn  (KHÔNG có MaTraHang -> tránh chu trình với TraHang)
CREATE TABLE HoaDon (
    MaHD           VARCHAR(20) PRIMARY KEY,
    ThoiGian       DATETIME NOT NULL,
    MaKH           VARCHAR(20),
    MaNV           INT,
    TongTienHang   DECIMAL(18,2) NOT NULL DEFAULT 0,
    GiamGia        DECIMAL(18,2) NOT NULL DEFAULT 0,
    TongSauGiamGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    TrangThai      ENUM('DA_TT','CHUA_TT') NOT NULL DEFAULT 'CHUA_TT',
    DaThanhToan    DECIMAL(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_hd_kh FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_hd_nv FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 10. Chi tiết hóa đơn (PK kép) - lưu GiaVon để tính lợi nhuận gộp
CREATE TABLE ChiTietHoaDon (
    MaHD     VARCHAR(20) NOT NULL,
    MaSP     VARCHAR(30) NOT NULL,
    SoLuong  INT NOT NULL,
    DonGia   DECIMAL(15,2) NOT NULL DEFAULT 0,
    GiaVon   DECIMAL(15,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (MaHD, MaSP),
    CONSTRAINT fk_cthd_hd FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cthd_sp FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 11. Thanh toán
CREATE TABLE ThanhToan (
    MaTT     INT AUTO_INCREMENT PRIMARY KEY,
    MaHD     VARCHAR(20) NOT NULL,
    SoTien   DECIMAL(18,2) NOT NULL DEFAULT 0,
    HinhThuc ENUM('TIEN_MAT','CHUYEN_KHOAN','THE') NOT NULL DEFAULT 'TIEN_MAT',
    ThoiGian DATETIME NOT NULL,
    CONSTRAINT fk_tt_hd FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 5: TRẢ HÀNG (khách trả) -- chỉ trỏ 1 chiều về HoaDon
-- =====================================================================

-- 12. Trả hàng
CREATE TABLE TraHang (
    MaTra        VARCHAR(20) PRIMARY KEY,
    MaHD         VARCHAR(20),                 -- NULL nếu trả nhanh không gắn hóa đơn
    MaKH         VARCHAR(20),
    MaNV         INT,
    ThoiGian     DATETIME NOT NULL,
    TongTienHang DECIMAL(18,2) NOT NULL DEFAULT 0,
    CanTraKhach  DECIMAL(18,2) NOT NULL DEFAULT 0,
    DaTraKhach   DECIMAL(18,2) NOT NULL DEFAULT 0,
    LyDo         VARCHAR(255),
    TrangThai    ENUM('DA_TRA','DA_HUY') NOT NULL DEFAULT 'DA_TRA',
    CONSTRAINT fk_th_hd FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_th_kh FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_th_nv FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 13. Chi tiết trả hàng (PK kép)
CREATE TABLE ChiTietTraHang (
    MaTra    VARCHAR(20) NOT NULL,
    MaSP     VARCHAR(30) NOT NULL,
    SoLuong  INT NOT NULL,
    DonGia   DECIMAL(15,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (MaTra, MaSP),
    CONSTRAINT fk_ctth_th FOREIGN KEY (MaTra) REFERENCES TraHang(MaTra)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ctth_sp FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 6: KHO
-- (gom Nhập hàng / Trả hàng nhập / Kiểm kho / Xuất hủy)
-- =====================================================================

-- 14. Giao dịch kho
CREATE TABLE GiaoDichKho (
    MaGD      VARCHAR(20) PRIMARY KEY,         -- PN.../THN.../KK...
    LoaiGD    ENUM('NHAP','TRA_NHAP','KIEM_KHO','XUAT_HUY') NOT NULL,
    MaNCC     VARCHAR(20),                     -- NULL với kiểm kho / xuất hủy
    MaNV      INT,
    ThoiGian  DATETIME NOT NULL,
    GhiChu    VARCHAR(255),
    TrangThai VARCHAR(30),
    CONSTRAINT fk_gdk_ncc FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_gdk_nv FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 14b. Chi tiết giao dịch kho (bảng phụ trợ - PK kép)
-- (SoLuong với KIEM_KHO là số lệch +/-)
CREATE TABLE ChiTietGiaoDichKho (
    MaGD        VARCHAR(20) NOT NULL,
    MaSP        VARCHAR(30) NOT NULL,
    SoLuong     INT NOT NULL,
    DonGiaNhap  DECIMAL(15,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (MaGD, MaSP),
    CONSTRAINT fk_ctgdk_gd FOREIGN KEY (MaGD) REFERENCES GiaoDichKho(MaGD)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ctgdk_sp FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- NHÓM 7: MỞ RỘNG
-- =====================================================================

-- 15. Yêu cầu sửa chữa
CREATE TABLE YeuCauSuaChua (
    MaYC      INT AUTO_INCREMENT PRIMARY KEY,
    MaKH      VARCHAR(20),
    MaSP      VARCHAR(30),
    TinhTrang VARCHAR(255),
    TrangThai ENUM('TIEP_NHAN','DANG_SUA','HOAN_THANH','HUY') NOT NULL DEFAULT 'TIEP_NHAN',
    ThoiGian  DATETIME NOT NULL,
    CONSTRAINT fk_yc_kh FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_yc_sp FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- 16. Vận đơn
CREATE TABLE VanDon (
    MaVD            VARCHAR(20) PRIMARY KEY,
    MaHD            VARCHAR(20),
    DiaChiGiao      VARCHAR(255),
    DonViVanChuyen  VARCHAR(100),
    TrangThaiGiao   ENUM('CHO_LAY','DANG_GIAO','DA_GIAO','HOAN') NOT NULL DEFAULT 'CHO_LAY',
    CONSTRAINT fk_vd_hd FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- 17. Nền tảng online
CREATE TABLE NenTangOnline (
    MaNT  INT AUTO_INCREMENT PRIMARY KEY,
    TenNT VARCHAR(50) NOT NULL           -- Shopee, Lazada, Tiki, Tiktok...
) ENGINE=InnoDB;

-- 18. Đơn hàng online
CREATE TABLE DonHangOnline (
    MaDHO     VARCHAR(30) PRIMARY KEY,
    MaNT      INT,
    MaHD      VARCHAR(20),               -- NULL cho tới khi sinh hóa đơn
    ThoiGian  DATETIME NOT NULL,
    TrangThai VARCHAR(30),
    CONSTRAINT fk_dho_nt FOREIGN KEY (MaNT) REFERENCES NenTangOnline(MaNT)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_dho_hd FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- KẾT THÚC SCHEMA
-- Tiếp theo chạy: 02_triggers.sql, 03_views.sql, 04_indexes.sql, 05_sample_data.sql
-- =====================================================================