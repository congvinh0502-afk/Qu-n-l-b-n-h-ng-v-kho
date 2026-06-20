-- =====================================================================
-- 07_fix_thanhtoan.sql
-- Sửa LOGIC THANH TOÁN của HÓA ĐƠN (chạy SAU 01..06).
-- Vấn đề: TrangThai chỉ có DA_TT/CHUA_TT -> hóa đơn trả MỘT PHẦN
--         bị đánh dấu sai là "Đã TT".
-- Giải pháp:
--   1) Thêm trạng thái MOT_PHAN + cột ConNo.
--   2) Trigger TỰ tính DaThanhToan / ConNo / TrangThai theo số tiền thực trả,
--      đảm bảo trạng thái LUÔN khớp, không gán tay sai được.
-- =====================================================================

USE qlbh;

-- 1) Mở rộng trạng thái + thêm cột Còn nợ
ALTER TABLE HoaDon
    MODIFY COLUMN TrangThai ENUM('CHUA_TT','MOT_PHAN','DA_TT') NOT NULL DEFAULT 'CHUA_TT';

ALTER TABLE HoaDon
    ADD COLUMN ConNo DECIMAL(18,2) NOT NULL DEFAULT 0;

-- 2) Gỡ trigger cũ nếu chạy lại
DROP TRIGGER IF EXISTS trg_hd_tinh_trangthai_ins;
DROP TRIGGER IF EXISTS trg_hd_tinh_trangthai_upd;
DROP TRIGGER IF EXISTS trg_tt_dongbo_hoadon_ins;
DROP TRIGGER IF EXISTS trg_tt_dongbo_hoadon_del;

-- ---------------------------------------------------------------------
-- (A) Khi TẠO hóa đơn: tự tính ConNo + TrangThai từ DaThanhToan
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_hd_tinh_trangthai_ins
BEFORE INSERT ON HoaDon
FOR EACH ROW
BEGIN
    SET NEW.ConNo = GREATEST(NEW.TongSauGiamGia - NEW.DaThanhToan, 0);
    IF NEW.DaThanhToan <= 0 THEN
        SET NEW.TrangThai = 'CHUA_TT';
    ELSEIF NEW.DaThanhToan < NEW.TongSauGiamGia THEN
        SET NEW.TrangThai = 'MOT_PHAN';
    ELSE
        SET NEW.TrangThai = 'DA_TT';
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (B) Khi hóa đơn ĐỔI (tổng tiền hoặc đã trả thay đổi): tính lại
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_hd_tinh_trangthai_upd
BEFORE UPDATE ON HoaDon
FOR EACH ROW
BEGIN
    SET NEW.ConNo = GREATEST(NEW.TongSauGiamGia - NEW.DaThanhToan, 0);
    IF NEW.DaThanhToan <= 0 THEN
        SET NEW.TrangThai = 'CHUA_TT';
    ELSEIF NEW.DaThanhToan < NEW.TongSauGiamGia THEN
        SET NEW.TrangThai = 'MOT_PHAN';
    ELSE
        SET NEW.TrangThai = 'DA_TT';
    END IF;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (C) Khi THÊM một lần thanh toán: cộng dồn tổng đã trả vào hóa đơn
--     (UPDATE này sẽ kích hoạt trigger (B) -> ConNo/TrangThai tự đúng)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_tt_dongbo_hoadon_ins
AFTER INSERT ON ThanhToan
FOR EACH ROW
BEGIN
    UPDATE HoaDon
    SET DaThanhToan = (
        SELECT COALESCE(SUM(SoTien),0) FROM ThanhToan WHERE MaHD = NEW.MaHD
    )
    WHERE MaHD = NEW.MaHD;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- (D) Khi XÓA một lần thanh toán: tính lại tổng đã trả
-- ---------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_tt_dongbo_hoadon_del
AFTER DELETE ON ThanhToan
FOR EACH ROW
BEGIN
    UPDATE HoaDon
    SET DaThanhToan = (
        SELECT COALESCE(SUM(SoTien),0) FROM ThanhToan WHERE MaHD = OLD.MaHD
    )
    WHERE MaHD = OLD.MaHD;
END //
DELIMITER ;

-- ---------------------------------------------------------------------
-- 3) Cập nhật lại các hóa đơn ĐÃ CÓ cho khớp logic mới
--    (UPDATE này kích hoạt trigger (B) tự tính ConNo + TrangThai)
-- ---------------------------------------------------------------------
UPDATE HoaDon SET DaThanhToan = DaThanhToan;

-- ---------------------------------------------------------------------
-- 4) Sửa trigger log hóa đơn: format tiền theo kiểu Việt Nam (1.200.000)
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_log_hd_them;
CREATE TRIGGER trg_log_hd_them
AFTER INSERT ON HoaDon FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
VALUES ('HoaDon','THEM',NEW.MaHD,
    CONCAT('Tao hoa don, tong tien: ',REPLACE(FORMAT(NEW.TongSauGiamGia,0),',','.'), ' d'),
    COALESCE(@app_user,'system'));

-- =====================================================================
-- KIỂM THỬ:
--   SELECT MaHD, TongSauGiamGia, DaThanhToan, ConNo, TrangThai FROM HoaDon;
--   -> Hóa đơn trả 1 phần phải hiện MOT_PHAN + ConNo > 0.
-- =====================================================================
