-- =====================================================================
-- 05_sample_data.sql
-- Dữ liệu mẫu để kiểm thử (số liệu phỏng theo ảnh KiotViet)
-- Chạy SAU 01_schema.sql + 02_triggers.sql.
-- LƯU Ý: vì có trigger tự trừ/cộng kho, thứ tự INSERT rất quan trọng:
--   1) Danh mục  2) Nhập kho (để có tồn)  3) Bán hàng  4) Trả hàng
-- TonKho sẽ do TRIGGER tự tính, KHÔNG set tay khi bán/nhập.
-- =====================================================================

USE qlbh;

-- ---------- 1) Danh mục ----------
INSERT INTO LoaiSanPham (TenLoai) VALUES
('Phớt'), ('Dây răng cưa'), ('Bơm mỡ'), ('Vòng bi'), ('Khác');

INSERT INTO NhaCungCap (MaNCC, TenNCC, DienThoai, DiaChi) VALUES
('NCC002', 'Nhà cung cấp 002', '0901000002', 'Hà Nội'),
('NCC17',  'Nhà cung cấp 17',  '0901000017', 'Hà Nội'),
('NCC109', 'Nhà cung cấp 109', '0901000109', 'Bắc Ninh'),
('NCC145', 'Nhà cung cấp 145', '0901000145', 'Hưng Yên');

-- Sản phẩm: ban đầu TonKho = 0, sẽ tăng khi nhập kho ở bước 2
INSERT INTO SanPham (MaSP, TenSP, MaLoai, GiaVon, GiaBan, TonKho) VALUES
('SP85133152', 'Bộ phớt tổng phanh SK100W (Bộ)', 1, 40000, 60000, 0),
('SP85133153', 'Dây Răng Cưa 8520 (BX52)',       2, 0,     0,     0),
('82014974',   'Phớt chịu nhiệt RWDR-KASSETTE (Cái)', 1, 700000, 1050000, 0),
('BRR1041-A0', 'PHỚT BỤI Y 43*54.4*4.6/14 NOK (Cái)', 1, 90000, 130000, 0),
('DCY435411',  'DCY 43*54*11 NOK (Cái)',         1, 8000,  15000, 0);

INSERT INTO KhachHang (MaKH, TenKH, DienThoai) VALUES
('KH000806', 'A THỨC - CH THỨC DUYÊN', '0901000806'),
('KH000373', 'ANH VIỆT - CTY VIỆT ANH', '0901000373'),
('KH000309', 'A TUẤN - QUẢNG NGÃI', '0901000309'),
('KH001242', 'Anh Minh - Cty Minh Phương', '0969788386');

INSERT INTO NhanVien (TenNV, DienThoai, ChucVu) VALUES
('Phan Thanh Bắc', '0902000001', 'Bán hàng'),
('Thúy Hiền',      '0902000002', 'Bán hàng'),
('Quản trị viên',  '0902000003', 'Quản lý');

-- Mật khẩu mẫu = chuỗi placeholder; thực tế ứng dụng sẽ ghi hash BCrypt
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, MaNV, VaiTro) VALUES
('admin', '$2a$10$PLACEHOLDERHASHADMIN0000000000000000000000000000', 3, 'QUANLY'),
('bac',   '$2a$10$PLACEHOLDERHASHBAC000000000000000000000000000000', 1, 'BANHANG');

-- ---------- 2) Nhập kho (trigger sẽ CỘNG tồn kho) ----------
INSERT INTO GiaoDichKho (MaGD, LoaiGD, MaNCC, MaNV, ThoiGian, TrangThai) VALUES
('PN016170', 'NHAP', 'NCC002', 3, '2026-03-20 17:30:00', 'Đã nhập hàng'),
('PN016169', 'NHAP', 'NCC17',  3, '2026-03-20 16:51:00', 'Đã nhập hàng');

INSERT INTO ChiTietGiaoDichKho (MaGD, MaSP, SoLuong, DonGiaNhap) VALUES
('PN016170', 'SP85133152', 100, 40000),
('PN016170', '82014974',   50,  700000),
('PN016170', 'BRR1041-A0', 300, 90000),
('PN016169', 'DCY435411',  500, 8000);
-- Sau bước này (do trigger): SP85133152=100, 82014974=50, BRR1041-A0=300, DCY435411=500

-- ---------- 3) Bán hàng (trigger CHẶN bán quá kho + tự TRỪ kho + cộng Tổng bán) ----------
INSERT INTO HoaDon (MaHD, ThoiGian, MaKH, MaNV, TongTienHang, GiamGia, TongSauGiamGia, TrangThai, DaThanhToan) VALUES
('HD056448', '2026-03-20 18:02:00', 'KH000806', 1, 435000,  0, 435000,  'DA_TT', 435000),
('HD056445', '2026-03-20 17:55:00', 'KH000373', 2, 530000,  0, 530000,  'DA_TT', 530000);

INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGia, GiaVon) VALUES
('HD056448', 'BRR1041-A0', 3,  130000, 90000),   -- 390.000
('HD056448', 'DCY435411',  3,  15000,  8000),    --  45.000  => 435.000
('HD056445', '82014974',   1,  1050000, 700000); -- (giả định giá bán lẻ ô này)
-- Sau bước này (do trigger): BRR1041-A0=297, DCY435411=497, 82014974=49

INSERT INTO ThanhToan (MaHD, SoTien, HinhThuc, ThoiGian) VALUES
('HD056448', 435000, 'TIEN_MAT',     '2026-03-20 18:02:00'),
('HD056445', 530000, 'CHUYEN_KHOAN', '2026-03-20 17:55:00');

-- ---------- 4) Khách trả hàng (trigger CỘNG lại kho + giảm Tổng bán trừ trả) ----------
INSERT INTO TraHang (MaTra, MaHD, MaKH, MaNV, ThoiGian, TongTienHang, CanTraKhach, DaTraKhach, LyDo, TrangThai) VALUES
('TH002971', 'HD056448', 'KH000806', 1, '2026-03-21 09:00:00', 15000, 15000, 15000, 'Khách muốn trả', 'DA_TRA');

INSERT INTO ChiTietTraHang (MaTra, MaSP, SoLuong, DonGia) VALUES
('TH002971', 'DCY435411', 1, 15000);
-- Sau bước này (do trigger): DCY435411 = 497 + 1 = 498

-- ---------- 5) Đặt hàng (để test view top đặt hàng) ----------
INSERT INTO DonDatHang (MaDH, ThoiGian, MaKH, MaNV, TongTienHang, GiamGia, TongSauGiamGia, TrangThai) VALUES
('DH053717', '2026-03-21 09:40:00', 'KH000806', 1, 550000, 0, 550000, 'PHIEU_TAM');

INSERT INTO ChiTietDonDatHang (MaDH, MaSP, SoLuong, DonGia) VALUES
('DH053717', 'BRR1041-A0', 2, 130000),
('DH053717', 'DCY435411',  20, 15000);

-- ---------- 6) Mở rộng: nền tảng online ----------
INSERT INTO NenTangOnline (TenNT) VALUES
('Shopee'), ('Lazada'), ('Tiki'), ('Tiktok Shop');

-- =====================================================================
-- KIỂM TRA NHANH SAU KHI CHẠY:
--   SELECT MaSP, TonKho FROM SanPham;          -- xem trigger cập nhật đúng chưa
--   SELECT MaKH, TongBan, TongBanTruTra FROM KhachHang;
--   SELECT * FROM v_phantich_sanpham;
--   SELECT * FROM v_doanhthu_ngay;
--   SELECT * FROM v_hoadon_matrahang;
-- =====================================================================
