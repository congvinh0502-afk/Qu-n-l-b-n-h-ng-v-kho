-- =====================================================================
-- 03_views.sql
-- View phục vụ các màn hình Phân tích / Báo cáo (khớp ảnh KiotViet)
-- Chạy SAU 01_schema.sql.
-- =====================================================================

USE qlbh;

DROP VIEW IF EXISTS v_doanhthu_ngay;
DROP VIEW IF EXISTS v_phantich_sanpham;
DROP VIEW IF EXISTS v_top_dathang;
DROP VIEW IF EXISTS v_hoadon_matrahang;
DROP VIEW IF EXISTS v_tongquan_hanghoa;

-- ---------------------------------------------------------------------
-- (A) Doanh thu thuần theo NGÀY  (ảnh 4: biểu đồ doanh thu tuần)
--     Lọc theo tuần ở tầng ứng dụng bằng WHERE Ngay BETWEEN ...
-- ---------------------------------------------------------------------
CREATE VIEW v_doanhthu_ngay AS
SELECT DATE(ThoiGian)       AS Ngay,
       SUM(TongSauGiamGia)  AS DoanhThuThuan,
       COUNT(*)             AS SoHoaDon
FROM HoaDon
GROUP BY DATE(ThoiGian);

-- ---------------------------------------------------------------------
-- (B) Phân tích theo từng SẢN PHẨM  (ảnh 6, ảnh 7)
--     Đã trừ hàng trả: SoLuongBan = bán - trả.
-- ---------------------------------------------------------------------
CREATE VIEW v_phantich_sanpham AS
SELECT
    sp.MaSP,
    sp.TenSP,
    COALESCE(b.SoLuongBan, 0)                            AS SoLuongBan,
    COALESCE(t.SoLuongTra, 0)                            AS SoLuongTra,
    COALESCE(b.SoLuongBan, 0) - COALESCE(t.SoLuongTra,0) AS SoLuongThuc,
    COALESCE(b.DoanhThu, 0)                              AS DoanhThu,
    COALESCE(b.LoiNhuanGop, 0)                           AS LoiNhuanGop
FROM SanPham sp
LEFT JOIN (
    SELECT MaSP,
           SUM(SoLuong)                        AS SoLuongBan,
           SUM(SoLuong * DonGia)               AS DoanhThu,
           SUM(SoLuong * (DonGia - GiaVon))    AS LoiNhuanGop
    FROM ChiTietHoaDon
    GROUP BY MaSP
) b ON b.MaSP = sp.MaSP
LEFT JOIN (
    SELECT MaSP, SUM(SoLuong) AS SoLuongTra
    FROM ChiTietTraHang
    GROUP BY MaSP
) t ON t.MaSP = sp.MaSP;

-- Dùng cho:
--   Top 10 doanh thu cao nhất:   SELECT * FROM v_phantich_sanpham ORDER BY DoanhThu DESC LIMIT 10;
--   Top 10 bán chạy theo SL:     SELECT * FROM v_phantich_sanpham ORDER BY SoLuongThuc DESC LIMIT 10;
--   Top 10 lợi nhuận:            SELECT * FROM v_phantich_sanpham ORDER BY LoiNhuanGop DESC LIMIT 10;
--   Bán chậm:                    ... ORDER BY SoLuongThuc ASC LIMIT 10;

-- ---------------------------------------------------------------------
-- (C) Top sản phẩm được ĐẶT nhiều nhất  (ảnh 13)
-- ---------------------------------------------------------------------
CREATE VIEW v_top_dathang AS
SELECT sp.MaSP, sp.TenSP, SUM(ct.SoLuong) AS TongDat
FROM ChiTietDonDatHang ct
JOIN SanPham sp ON sp.MaSP = ct.MaSP
GROUP BY sp.MaSP, sp.TenSP;
-- Dùng:  SELECT * FROM v_top_dathang ORDER BY TongDat DESC LIMIT 10;

-- ---------------------------------------------------------------------
-- (D) Hóa đơn kèm "Mã trả hàng" (JOIN thay cho FK ngược - tránh chu trình)
--     Tái hiện cột "Mã trả hàng" trong màn hình Hóa đơn (ảnh 2).
-- ---------------------------------------------------------------------
CREATE VIEW v_hoadon_matrahang AS
SELECT
    hd.MaHD,
    hd.ThoiGian,
    hd.MaKH,
    kh.TenKH,
    hd.TongTienHang,
    hd.GiamGia,
    hd.TongSauGiamGia,
    hd.TrangThai,
    t.MaTra AS MaTraHang          -- NULL nếu hóa đơn này chưa có phiếu trả
FROM HoaDon hd
LEFT JOIN KhachHang kh ON kh.MaKH = hd.MaKH
LEFT JOIN TraHang   t  ON t.MaHD  = hd.MaHD;

-- ---------------------------------------------------------------------
-- (E) Tổng quan hàng hóa toàn hệ thống  (4 ô số liệu ở ảnh 6)
-- ---------------------------------------------------------------------
CREATE VIEW v_tongquan_hanghoa AS
SELECT
    COUNT(DISTINCT MaSP)                          AS SoMatHangDaBan,
    SUM(SoLuong)                                  AS SoLuongThucBan,
    ROUND(AVG(SoLuong * DonGia), 2)               AS DoanhThuTB_SP,
    ROUND(AVG(SoLuong * (DonGia - GiaVon)), 2)    AS LoiNhuanGopTB_SP
FROM ChiTietHoaDon;
