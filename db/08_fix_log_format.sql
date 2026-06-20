-- =====================================================================
-- 08_fix_log_format.sql
-- Sửa trigger nhật ký: format tiền theo kiểu Việt Nam (1.200.000 đ)
-- Chạy MỘT LẦN sau 06_upgrades.sql và 07_fix_thanhtoan.sql
-- =====================================================================

USE qlbh;

-- Sửa trigger log hóa đơn: 1200000.00 -> 1.200.000 d
DROP TRIGGER IF EXISTS trg_log_hd_them;
CREATE TRIGGER trg_log_hd_them
AFTER INSERT ON HoaDon FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
VALUES ('HoaDon','THEM',NEW.MaHD,
    CONCAT('Tao hoa don, tong tien: ', REPLACE(FORMAT(NEW.TongSauGiamGia,0),',','.'), ' d'),
    COALESCE(@app_user,'system'));

-- Kiểm tra: SHOW TRIGGERS LIKE 'HoaDon';
