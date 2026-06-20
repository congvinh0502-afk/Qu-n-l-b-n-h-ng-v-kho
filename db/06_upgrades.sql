-- =====================================================================
-- 06_upgrades.sql
-- Thay đổi CSDL cho các tính năng NÂNG CẤP (chạy SAU 01..05).
-- Gom mọi cột mới / bảng mới / view mới / trigger log vào 1 file,
-- chạy MỘT LẦN trước khi làm các tính năng nâng cấp.
-- =====================================================================

USE qlbh;

-- ---------------------------------------------------------------------
-- [Tính năng 2 - Cảnh báo tồn kho] thêm mức tồn tối thiểu cho từng SP
-- ---------------------------------------------------------------------
ALTER TABLE SanPham
    ADD COLUMN MucTonToiThieu INT NOT NULL DEFAULT 10;

DROP VIEW IF EXISTS v_hang_sap_het;
CREATE VIEW v_hang_sap_het AS
SELECT MaSP, TenSP, TonKho, MucTonToiThieu
FROM SanPham
WHERE TrangThai = 1 AND TonKho <= MucTonToiThieu;

-- ---------------------------------------------------------------------
-- [Tính năng 3 - Bán online] trạng thái kết nối + màu thương hiệu
-- ---------------------------------------------------------------------
ALTER TABLE NenTangOnline
    ADD COLUMN TrangThaiKetNoi TINYINT NOT NULL DEFAULT 0,  -- 0=chưa, 1=đã kết nối
    ADD COLUMN MauSac VARCHAR(20);                          -- mã màu icon, vd '#EE4D2D'

UPDATE NenTangOnline SET MauSac = '#EE4D2D' WHERE TenNT = 'Shopee';
UPDATE NenTangOnline SET MauSac = '#0F146D' WHERE TenNT = 'Lazada';
UPDATE NenTangOnline SET MauSac = '#1A94FF' WHERE TenNT = 'Tiki';
UPDATE NenTangOnline SET MauSac = '#000000' WHERE TenNT = 'Tiktok Shop';

-- thêm cột phục vụ hiển thị bảng đơn online (mô phỏng vận chuyển)
ALTER TABLE DonHangOnline
    ADD COLUMN DiaChiGiao VARCHAR(255),
    ADD COLUMN DonViVanChuyen VARCHAR(100),
    ADD COLUMN TrangThaiVC ENUM('CHO_LAY','DANG_GIAO','DA_GIAO','HOAN') DEFAULT 'CHO_LAY';

-- ---------------------------------------------------------------------
-- [Tính năng 4 - Nhật ký hoạt động / Audit log]
-- Bảng log + trigger tự ghi. Người dùng lấy từ biến phiên @app_user
-- (ứng dụng set 'SET @app_user = ten_dang_nhap' ngay sau khi đăng nhập).
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS NhatKyHoatDong;
CREATE TABLE NhatKyHoatDong (
    MaLog       BIGINT AUTO_INCREMENT PRIMARY KEY,
    BangTacDong VARCHAR(50)  NOT NULL,
    HanhDong    ENUM('THEM','SUA','XOA') NOT NULL,
    MaBanGhi    VARCHAR(50),
    MoTa        VARCHAR(500),
    NguoiDung   VARCHAR(50)  NOT NULL DEFAULT 'system',
    ThoiGian    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_log_thoigian ON NhatKyHoatDong(ThoiGian);

DROP TRIGGER IF EXISTS trg_log_sp_them;
DROP TRIGGER IF EXISTS trg_log_sp_sua;
DROP TRIGGER IF EXISTS trg_log_sp_xoa;
DROP TRIGGER IF EXISTS trg_log_hd_them;

-- Thêm sản phẩm
CREATE TRIGGER trg_log_sp_them
AFTER INSERT ON SanPham
FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong, HanhDong, MaBanGhi, MoTa, NguoiDung)
VALUES ('SanPham','THEM', NEW.MaSP,
        CONCAT('Them san pham: ', NEW.TenSP),
        COALESCE(@app_user,'system'));

-- Xóa sản phẩm
CREATE TRIGGER trg_log_sp_xoa
AFTER DELETE ON SanPham
FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong, HanhDong, MaBanGhi, MoTa, NguoiDung)
VALUES ('SanPham','XOA', OLD.MaSP,
        CONCAT('Xoa san pham: ', OLD.TenSP),
        COALESCE(@app_user,'system'));

-- Tạo hóa đơn
CREATE TRIGGER trg_log_hd_them
AFTER INSERT ON HoaDon
FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong, HanhDong, MaBanGhi, MoTa, NguoiDung)
VALUES ('HoaDon','THEM', NEW.MaHD,
        CONCAT('Tao hoa don, tong tien: ', REPLACE(FORMAT(NEW.TongSauGiamGia,0),',','.'), ' d'),
        COALESCE(@app_user,'system'));

-- Sửa sản phẩm: CHỈ log khi đổi Tên / Giá / Trạng thái
-- (bỏ qua thay đổi TonKho do trigger bán/nhập hàng gây ra -> tránh log rác)
DELIMITER //
CREATE TRIGGER trg_log_sp_sua
AFTER UPDATE ON SanPham
FOR EACH ROW
BEGIN
    IF (NEW.TenSP <> OLD.TenSP)
       OR (NEW.GiaBan <> OLD.GiaBan)
       OR (NEW.GiaVon <> OLD.GiaVon)
       OR (NEW.TrangThai <> OLD.TrangThai) THEN
        INSERT INTO NhatKyHoatDong(BangTacDong, HanhDong, MaBanGhi, MoTa, NguoiDung)
        VALUES ('SanPham','SUA', NEW.MaSP,
                CONCAT('Sua san pham: ', NEW.TenSP),
                COALESCE(@app_user,'system'));
    END IF;
END //
DELIMITER ;

-- =====================================================================
-- GHI CHÚ:
--  - Tính năng 1 (Dashboard) và 5,6,7 KHÔNG cần đổi CSDL.
--  - Ứng dụng nên chạy: SET @app_user = '<ten_dang_nhap>';  sau khi đăng nhập,
--    để cột NguoiDung trong log ghi đúng tên người thao tác.
--  - Chỉ vai trò QUANLY mới được xem màn hình Nhật ký hoạt động.
-- =====================================================================
