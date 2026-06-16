# NÂNG CẤP PHẦN MỀM — Tính năng mở rộng sau 9 giai đoạn cốt lõi

> File này dùng cho Claude Code đọc, NỐI TIẾP `ROADMAP.md` và `CLAUDE.md`.
> **Chỉ làm sau khi 9 giai đoạn cốt lõi đã chạy ổn** (phần mềm bán hàng đã chạy trọn vòng đời).
> Làm **từng tính năng một** theo thứ tự dưới đây, xong cái nào chạy thử cái đó rồi mới sang cái sau.

---

## BƯỚC 0 — CHẠY TRƯỚC: cập nhật cơ sở dữ liệu

Một số tính năng cần thêm cột / bảng / trigger. Tất cả gom vào **một file SQL chạy một lần**.
Phần SQL dưới đây đã được kiểm thử thật trên MySQL 8 (chạy sạch, trigger log hoạt động đúng).

**Prompt cho Claude Code:**
> Tạo file `db/06_upgrades.sql` với đúng nội dung trong khối SQL dưới đây, sau đó chạy nó vào database `qlbh` (chạy SAU các file 01–05). Kiểm tra lại bằng `SELECT * FROM v_hang_sap_het;` và `SHOW TRIGGERS;`.

```sql
-- =====================================================================
-- 06_upgrades.sql  (chạy SAU 01..05)
-- =====================================================================
USE qlbh;

-- [Cảnh báo tồn kho] mức tồn tối thiểu cho từng SP
ALTER TABLE SanPham
    ADD COLUMN MucTonToiThieu INT NOT NULL DEFAULT 10;

DROP VIEW IF EXISTS v_hang_sap_het;
CREATE VIEW v_hang_sap_het AS
SELECT MaSP, TenSP, TonKho, MucTonToiThieu
FROM SanPham
WHERE TrangThai = 1 AND TonKho <= MucTonToiThieu;

-- [Bán online] trạng thái kết nối + màu thương hiệu + cột hiển thị vận chuyển
ALTER TABLE NenTangOnline
    ADD COLUMN TrangThaiKetNoi TINYINT NOT NULL DEFAULT 0,  -- 0=chưa, 1=đã kết nối
    ADD COLUMN MauSac VARCHAR(20);

UPDATE NenTangOnline SET MauSac = '#EE4D2D' WHERE TenNT = 'Shopee';
UPDATE NenTangOnline SET MauSac = '#0F146D' WHERE TenNT = 'Lazada';
UPDATE NenTangOnline SET MauSac = '#1A94FF' WHERE TenNT = 'Tiki';
UPDATE NenTangOnline SET MauSac = '#000000' WHERE TenNT = 'Tiktok Shop';

ALTER TABLE DonHangOnline
    ADD COLUMN DiaChiGiao VARCHAR(255),
    ADD COLUMN DonViVanChuyen VARCHAR(100),
    ADD COLUMN TrangThaiVC ENUM('CHO_LAY','DANG_GIAO','DA_GIAO','HOAN') DEFAULT 'CHO_LAY';

-- [Nhật ký hoạt động] bảng log + trigger tự ghi
-- Người dùng lấy từ biến phiên @app_user (app set sau khi đăng nhập)
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

CREATE TRIGGER trg_log_sp_them
AFTER INSERT ON SanPham FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
VALUES ('SanPham','THEM',NEW.MaSP,CONCAT('Them san pham: ',NEW.TenSP),COALESCE(@app_user,'system'));

CREATE TRIGGER trg_log_sp_xoa
AFTER DELETE ON SanPham FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
VALUES ('SanPham','XOA',OLD.MaSP,CONCAT('Xoa san pham: ',OLD.TenSP),COALESCE(@app_user,'system'));

CREATE TRIGGER trg_log_hd_them
AFTER INSERT ON HoaDon FOR EACH ROW
INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
VALUES ('HoaDon','THEM',NEW.MaHD,CONCAT('Tao hoa don, tong tien: ',NEW.TongSauGiamGia),COALESCE(@app_user,'system'));

-- Sửa SP: chỉ log khi đổi Tên/Giá/Trạng thái (bỏ qua thay đổi TonKho -> tránh log rác)
DELIMITER //
CREATE TRIGGER trg_log_sp_sua
AFTER UPDATE ON SanPham FOR EACH ROW
BEGIN
    IF (NEW.TenSP<>OLD.TenSP) OR (NEW.GiaBan<>OLD.GiaBan)
       OR (NEW.GiaVon<>OLD.GiaVon) OR (NEW.TrangThai<>OLD.TrangThai) THEN
        INSERT INTO NhatKyHoatDong(BangTacDong,HanhDong,MaBanGhi,MoTa,NguoiDung)
        VALUES ('SanPham','SUA',NEW.MaSP,CONCAT('Sua san pham: ',NEW.TenSP),COALESCE(@app_user,'system'));
    END IF;
END //
DELIMITER ;
```

> **Quan trọng cho phần Java:** ngay sau khi đăng nhập thành công, ứng dụng chạy câu lệnh
> `SET @app_user = '<tên đăng nhập>';` trên cùng connection — để cột `NguoiDung` trong log ghi đúng người thao tác.
> Nếu dùng connection pool (HikariCP), set biến này ở đầu mỗi giao dịch ghi dữ liệu.

---

## THỨ TỰ LÀM 7 TÍNH NĂNG (ưu tiên cao → thấp)

| # | Tính năng | Cần đổi DB? | Giá trị |
|---|---|---|---|
| 1 | Dashboard tổng quan | Không | Ấn tượng khi demo, dùng nhiều view |
| 2 | Cảnh báo tồn kho thấp | Có (đã ở B0) | Nghiệp vụ thật, ăn điểm |
| 3 | Hoàn thiện Bán online (sticker + kết nối) | Có (đã ở B0) | Đúng yêu cầu đề |
| 4 | Nhật ký hoạt động (Audit log) | Có (đã ở B0) | Thể hiện trigger nâng cao |
| 5 | Xuất Excel | Không | Tiện dụng, thực tế |
| 6 | In / Xuất PDF hóa đơn | Không | Thực tế, ấn tượng |
| 7 | Giao diện Sáng/Tối | Không | Làm đẹp |

---

## TÍNH NĂNG 1 — Dashboard tổng quan

**Mục tiêu:** màn hình đầu tiên sau đăng nhập, cho thấy bức tranh toàn cảnh.

**Prompt cho Claude Code:**
> Thêm màn hình "Tổng quan" làm trang mặc định sau đăng nhập, đặt trên cùng menu trái (trên mục Hàng hóa). Gồm:
> - 4 ô thống kê (card) lấy số liệu thật từ DB: **Doanh thu hôm nay** (tổng TongSauGiamGia của HoaDon trong ngày), **Số hóa đơn hôm nay**, **Số hàng sắp hết** (đếm từ view `v_hang_sap_het`), **Tổng số khách hàng**.
> - Một biểu đồ cột **doanh thu 7 ngày gần nhất** dùng view `v_doanhthu_ngay` (JavaFX BarChart).
> - Một bảng nhỏ **Top 5 sản phẩm bán chạy** dùng view `v_phantich_sanpham` sắp xếp theo SoLuongThuc.
> Mỗi card là một ô bo góc, có màu nền nhạt và số liệu cỡ lớn. Làm xong chạy `mvn javafx:run` rồi báo lại.

**Kiểm thử:** đăng nhập → thấy Dashboard với số liệu khớp dữ liệu mẫu, biểu đồ có cột.

---

## TÍNH NĂNG 2 — Cảnh báo tồn kho thấp

**Mục tiêu:** nhắc nhập hàng trước khi hết. (Cột `MucTonToiThieu` và view `v_hang_sap_het` đã thêm ở Bước 0.)

**Prompt cho Claude Code:**
> Trong màn Hàng hóa:
> - Tô **màu đỏ/cam** cho dòng nào có `TonKho <= MucTonToiThieu` (dùng rowFactory của TableView).
> - Thêm cột **"Mức tối thiểu"** (sửa được, lưu vào cột `MucTonToiThieu`).
> - Thêm ô lọc/checkbox **"Chỉ hiện hàng sắp hết"** — khi bật thì truy vấn từ view `v_hang_sap_het`.
> - Thêm một **chuông thông báo** ở thanh trên: hiển thị số lượng mặt hàng đang sắp hết, bấm vào hiện danh sách.

**Kiểm thử:** đặt `MucTonToiThieu` cao hơn TonKho của 1 SP → dòng đó đỏ + xuất hiện trong bộ lọc + đếm vào chuông.

---

## TÍNH NĂNG 3 — Hoàn thiện màn Bán online (sticker + liên kết)

**Mục tiêu:** đúng ý muốn ban đầu — các nền tảng có icon, trạng thái kết nối, đơn online gắn nền tảng.
(Cột `TrangThaiKetNoi`, `MauSac`, và các cột vận chuyển đã thêm ở Bước 0.)

**Prompt cho Claude Code:**
> Nâng cấp màn Bán online:
> - Phía trên bảng, thêm hàng **thẻ nền tảng** cho Shopee, Lazada, Tiki, Tiktok Shop. Mỗi thẻ có: icon tròn tô màu lấy từ cột `MauSac` + chữ cái đầu nền tảng, tên nền tảng, nhãn trạng thái **"Đã kết nối / Chưa kết nối"** (đọc cột `TrangThaiKetNoi`), và nút **Kết nối/Ngắt** để bật tắt (UPDATE cột đó).
> - Bảng đơn online hiển thị thêm cột: icon + tên nền tảng, Địa chỉ giao, Đơn vị VC, Trạng thái VC.
> - Nút **"Tạo đơn mô phỏng"**: mở form chọn nền tảng + khách + sản phẩm, khi lưu thì tạo bản ghi `DonHangOnline` và sinh `HoaDon` tương ứng (qua transaction) — để trigger tự trừ kho, chứng minh đồng bộ tồn kho giữa kênh online và offline.
>
> **Lưu ý bản quyền:** KHÔNG tải logo thật của Shopee/Lazada/Tiki/Tiktok từ web (có bản quyền). Tự vẽ icon đơn giản bằng hình tròn + chữ cái + màu thương hiệu trong cột `MauSac`.

**Kiểm thử:** bấm Kết nối một nền tảng → nhãn đổi trạng thái; tạo đơn mô phỏng → tồn kho của SP trong đơn giảm đúng.

---

## TÍNH NĂNG 4 — Nhật ký hoạt động (Audit log)

**Mục tiêu:** ghi lại ai làm gì, lúc nào. (Bảng `NhatKyHoatDong` + 4 trigger đã tạo ở Bước 0.)

**Prompt cho Claude Code:**
> - Ngay sau khi đăng nhập, chạy `SET @app_user = '<tên đăng nhập>'` trên connection để các trigger log ghi đúng người dùng. Nếu dùng HikariCP, đảm bảo lệnh này chạy đầu mỗi giao dịch ghi.
> - Thêm màn hình **"Nhật ký hoạt động"** ở nhóm BÁO CÁO trong menu, hiển thị bảng `NhatKyHoatDong` (thời gian, người dùng, bảng, hành động, mô tả), có lọc theo ngày và theo người dùng.
> - **Chỉ vai trò QUANLY** mới thấy mục này (ẩn với BANHANG/KHO/CSKH).

**Kiểm thử:** đăng nhập bằng admin → thêm/sửa/xóa một SP, tạo 1 hóa đơn → vào Nhật ký thấy đủ các dòng log mang tên admin.

---

## TÍNH NĂNG 5 — Xuất Excel

**Mục tiêu:** xuất dữ liệu đang xem ra file .xlsx. (Không đổi DB.)

**Prompt cho Claude Code:**
> Thêm thư viện Apache POI vào `pom.xml`. Thêm nút **"Xuất Excel"** ở màn Hàng hóa, Khách hàng và Phân tích. Khi bấm: xuất đúng dữ liệu đang hiển thị (kể cả sau khi lọc/tìm) ra file .xlsx, mở hộp thoại chọn nơi lưu (FileChooser), header in đậm và đóng băng dòng tiêu đề.

**Kiểm thử:** lọc danh sách rồi Xuất Excel → mở file thấy đúng dữ liệu đang xem.

---

## TÍNH NĂNG 6 — In / Xuất PDF hóa đơn

**Mục tiêu:** in hoặc lưu hóa đơn ra PDF. (Không đổi DB.)

**Prompt cho Claude Code:**
> Ở màn Bán hàng (hoặc danh sách Hóa đơn), thêm nút **"In hóa đơn"**. Khi bấm: tạo bản xem trước hóa đơn (tên cửa hàng, mã HĐ, ngày, tên khách, bảng danh sách SP + số lượng + đơn giá + thành tiền, tổng tiền, người bán). Cho phép **in** (JavaFX PrinterJob) hoặc **lưu PDF**. Định dạng tiền dạng 1,000,000.

**Kiểm thử:** chọn một hóa đơn → In/Lưu PDF → file hiển thị đúng nội dung hóa đơn.

---

## TÍNH NĂNG 7 — Giao diện Sáng/Tối (Dark mode)

**Mục tiêu:** đổi tông màu cho đẹp. (Không đổi DB.)

**Prompt cho Claude Code:**
> Tạo 2 file CSS: `light.css` (hiện tại) và `dark.css` (nền tối, chữ sáng, giữ màu nhấn xanh). Thêm nút/biểu tượng chuyển Sáng↔Tối ở thanh trên cùng; khi bấm thì đổi stylesheet cho toàn bộ Scene. Nhớ chỉnh để bảng, card, chữ đều đọc tốt ở chế độ tối.

**Kiểm thử:** bấm nút → toàn app đổi tông, không có chỗ nào chữ trùng màu nền.

---

## NGUYÊN TẮC CHUNG (nhắc lại)
- Làm **một tính năng một lần**, xong chạy thử (`mvn javafx:run`) rồi mới sang cái sau.
- Tính năng nào sửa DB thì đã gom hết ở **Bước 0** — chạy 06_upgrades.sql trước là đủ.
- Giữ đúng kiến trúc 3 tầng và quy ước trong `CLAUDE.md`.
- Ưu tiên 1→4 trước (vừa đẹp vừa thể hiện kiến thức CSDL); 5→7 làm thêm nếu còn thời gian.
