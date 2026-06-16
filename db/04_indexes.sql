-- =====================================================================
-- 04_indexes.sql
-- Index tối ưu tìm kiếm / lọc / báo cáo - khớp với 01_schema.sql
-- Chạy SAU 01_schema.sql.
-- (PK và FK đã tự có index; file này thêm index cho các cột HAY tìm kiếm.)
-- =====================================================================

USE qlbh;

-- Tìm kiếm hàng hóa theo tên (ô "Theo mã, tên hàng" - ảnh 3)
CREATE INDEX idx_sp_ten      ON SanPham(TenSP);

-- Tìm kiếm khách hàng theo tên / số điện thoại (ảnh 5)
CREATE INDEX idx_kh_ten      ON KhachHang(TenKH);
CREATE INDEX idx_kh_sdt      ON KhachHang(DienThoai);

-- Lọc / báo cáo theo thời gian (doanh thu theo ngày-tuần, ảnh 4)
CREATE INDEX idx_hd_thoigian  ON HoaDon(ThoiGian);
CREATE INDEX idx_ddh_thoigian ON DonDatHang(ThoiGian);
CREATE INDEX idx_th_thoigian  ON TraHang(ThoiGian);
CREATE INDEX idx_gdk_thoigian ON GiaoDichKho(ThoiGian);

-- Lọc giao dịch kho theo loại (Nhập / Trả nhập / Kiểm kho / Xuất hủy)
CREATE INDEX idx_gdk_loai     ON GiaoDichKho(LoaiGD);

-- Tăng tốc các view phân tích (gom nhóm theo sản phẩm)
CREATE INDEX idx_cthd_sp      ON ChiTietHoaDon(MaSP);
CREATE INDEX idx_ctth_sp      ON ChiTietTraHang(MaSP);
CREATE INDEX idx_ctddh_sp     ON ChiTietDonDatHang(MaSP);

-- =====================================================================
-- Kiểm tra index đã tạo:  SHOW INDEX FROM SanPham;
-- So sánh trước/sau:      EXPLAIN SELECT * FROM SanPham WHERE TenSP LIKE 'Day%';
-- =====================================================================
