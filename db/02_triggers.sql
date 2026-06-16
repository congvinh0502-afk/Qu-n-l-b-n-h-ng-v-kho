-- =====================================================================
-- 02_triggers.sql
-- Trigger tự động hóa nghiệp vụ - khớp với 01_schema.sql
-- Chạy SAU 01_schema.sql.
-- =====================================================================

USE qlbh;

DROP TRIGGER IF EXISTS trg_check_tonkho;
DROP TRIGGER IF EXISTS trg_banhang_trutonkho;
DROP TRIGGER IF EXISTS trg_kho_capnhat_tonkho;
DROP TRIGGER IF EXISTS trg_trahang_conghangtonkho;
DROP TRIGGER IF EXISTS trg_capnhat_tongban;
DROP TRIGGER IF EXISTS trg_trahang_giamtongban;

-- ---------------------------------------------------------------------
-- (1) Chặn bán quá tồn kho  (BEFORE INSERT chi tiết hóa đơn)
--     Phải đặt BEFORE để chặn trước khi trừ kho.
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_check_tonkho
BEFORE INSERT ON ChiTietHoaDon
FOR EACH ROW
BEGIN
    DECLARE ton INT;
    SELECT TonKho INTO ton FROM SanPham WHERE MaSP = NEW.MaSP;
    IF ton IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'San pham khong ton tai!';
    ELSEIF ton < NEW.SoLuong THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khong du ton kho de ban!';
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (2) Bán hàng -> tự trừ tồn kho  (AFTER INSERT chi tiết hóa đơn)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_banhang_trutonkho
AFTER INSERT ON ChiTietHoaDon
FOR EACH ROW
BEGIN
    UPDATE SanPham
    SET TonKho = TonKho - NEW.SoLuong
    WHERE MaSP = NEW.MaSP;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (3) Giao dịch kho -> cập nhật tồn kho theo loại giao dịch
--     NHAP        : +SoLuong
--     TRA_NHAP    : -SoLuong (trả lại cho NCC)
--     XUAT_HUY    : -SoLuong
--     KIEM_KHO    : +SoLuong (SoLuong là số lệch +/-)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_kho_capnhat_tonkho
AFTER INSERT ON ChiTietGiaoDichKho
FOR EACH ROW
BEGIN
    DECLARE loai VARCHAR(20);
    SELECT LoaiGD INTO loai FROM GiaoDichKho WHERE MaGD = NEW.MaGD;

    IF loai = 'NHAP' THEN
        UPDATE SanPham SET TonKho = TonKho + NEW.SoLuong WHERE MaSP = NEW.MaSP;
    ELSEIF loai = 'TRA_NHAP' OR loai = 'XUAT_HUY' THEN
        UPDATE SanPham SET TonKho = TonKho - NEW.SoLuong WHERE MaSP = NEW.MaSP;
    ELSEIF loai = 'KIEM_KHO' THEN
        UPDATE SanPham SET TonKho = TonKho + NEW.SoLuong WHERE MaSP = NEW.MaSP;
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (4) Khách trả hàng -> cộng lại tồn kho  (AFTER INSERT chi tiết trả hàng)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_trahang_conghangtonkho
AFTER INSERT ON ChiTietTraHang
FOR EACH ROW
BEGIN
    UPDATE SanPham
    SET TonKho = TonKho + NEW.SoLuong
    WHERE MaSP = NEW.MaSP;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (5) Tạo hóa đơn -> cộng "Tổng bán" và "Tổng bán trừ trả hàng" của khách
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_capnhat_tongban
AFTER INSERT ON HoaDon
FOR EACH ROW
BEGIN
    IF NEW.MaKH IS NOT NULL THEN
        UPDATE KhachHang
        SET TongBan       = TongBan + NEW.TongSauGiamGia,
            TongBanTruTra = TongBanTruTra + NEW.TongSauGiamGia
        WHERE MaKH = NEW.MaKH;
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (6) Khách trả hàng -> giảm "Tổng bán trừ trả hàng" của khách
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_trahang_giamtongban
AFTER INSERT ON TraHang
FOR EACH ROW
BEGIN
    IF NEW.MaKH IS NOT NULL THEN
        UPDATE KhachHang
        SET TongBanTruTra = TongBanTruTra - NEW.TongTienHang
        WHERE MaKH = NEW.MaKH;
    END IF;
END //
DELIMITER ;

-- =====================================================================
-- GHI CHÚ KIỂM THỬ:
--  - INSERT 1 dòng ChiTietHoaDon với SoLuong > TonKho  -> phải báo lỗi (1).
--  - INSERT chi tiết hóa đơn hợp lệ -> TonKho của SanPham giảm đúng (2).
--  - INSERT GiaoDichKho loại NHAP + chi tiết -> TonKho tăng (3).
--  - INSERT TraHang + ChiTietTraHang -> TonKho tăng (4) & TongBanTruTra giảm (6).
-- =====================================================================
