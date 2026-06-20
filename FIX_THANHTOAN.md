# SỬA LOGIC THANH TOÁN HÓA ĐƠN

> File cho Claude Code đọc. Mục tiêu: hóa đơn trả MỘT PHẦN không còn bị đánh dấu sai là "Đã TT".
> Gồm 2 việc: **(A) cập nhật CSDL + trigger** và **(B) sửa phần Java/giao diện**.
> Phần SQL dưới đây đã được kiểm thử thật trên MySQL 8 (logic 3 trạng thái + công nợ chạy đúng).

---

## VẤN ĐỀ
Hiện `HoaDon.TrangThai` chỉ có 2 giá trị (DA_TT / CHUA_TT). Khi khách trả thiếu (ví dụ tổng 1.200.000 nhưng mới trả 600.000), hóa đơn vẫn bị hiển thị "Đã TT" — sai. Thiếu cả khái niệm **công nợ (còn nợ)**.

## NGUYÊN TẮC THIẾT KẾ (quan trọng — phải tuân thủ ở phần Java)
**Bảng `ThanhToan` là nguồn dữ liệu DUY NHẤT cho số tiền đã trả.**
- Mỗi lần khách trả tiền (kể cả lần đầu khi tạo hóa đơn) → **ghi một dòng vào `ThanhToan`**.
- Khi tạo hóa đơn, đặt `DaThanhToan = 0`; **KHÔNG** set `DaThanhToan` trực tiếp.
- Trigger sẽ tự cộng dồn `ThanhToan` → cập nhật `DaThanhToan`, `ConNo`, `TrangThai`.
(Nếu set tay `DaThanhToan` mà không ghi vào `ThanhToan` sẽ gây lệch số liệu.)

---

## VIỆC A — CẬP NHẬT CƠ SỞ DỮ LIỆU

**Prompt:**
> Tạo file `db/07_fix_thanhtoan.sql` với đúng nội dung khối SQL dưới đây, rồi chạy vào database `qlbh` (chạy SAU các file 01–06). Sau đó kiểm tra: `SELECT MaHD, TongSauGiamGia, DaThanhToan, ConNo, TrangThai FROM HoaDon;`

```sql
-- 07_fix_thanhtoan.sql  (chạy SAU 01..06)
USE qlbh;

-- 1) Mở rộng trạng thái + thêm cột Còn nợ
ALTER TABLE HoaDon
    MODIFY COLUMN TrangThai ENUM('CHUA_TT','MOT_PHAN','DA_TT') NOT NULL DEFAULT 'CHUA_TT';
ALTER TABLE HoaDon
    ADD COLUMN ConNo DECIMAL(18,2) NOT NULL DEFAULT 0;

-- 2) Trigger
DROP TRIGGER IF EXISTS trg_hd_tinh_trangthai_ins;
DROP TRIGGER IF EXISTS trg_hd_tinh_trangthai_upd;
DROP TRIGGER IF EXISTS trg_tt_dongbo_hoadon_ins;
DROP TRIGGER IF EXISTS trg_tt_dongbo_hoadon_del;

-- (A) Tạo hóa đơn -> tự tính ConNo + TrangThai
DELIMITER //
CREATE TRIGGER trg_hd_tinh_trangthai_ins
BEFORE INSERT ON HoaDon FOR EACH ROW
BEGIN
    SET NEW.ConNo = GREATEST(NEW.TongSauGiamGia - NEW.DaThanhToan, 0);
    IF NEW.DaThanhToan <= 0 THEN SET NEW.TrangThai = 'CHUA_TT';
    ELSEIF NEW.DaThanhToan < NEW.TongSauGiamGia THEN SET NEW.TrangThai = 'MOT_PHAN';
    ELSE SET NEW.TrangThai = 'DA_TT';
    END IF;
END //
DELIMITER ;

-- (B) Hóa đơn đổi tổng tiền / đã trả -> tính lại
DELIMITER //
CREATE TRIGGER trg_hd_tinh_trangthai_upd
BEFORE UPDATE ON HoaDon FOR EACH ROW
BEGIN
    SET NEW.ConNo = GREATEST(NEW.TongSauGiamGia - NEW.DaThanhToan, 0);
    IF NEW.DaThanhToan <= 0 THEN SET NEW.TrangThai = 'CHUA_TT';
    ELSEIF NEW.DaThanhToan < NEW.TongSauGiamGia THEN SET NEW.TrangThai = 'MOT_PHAN';
    ELSE SET NEW.TrangThai = 'DA_TT';
    END IF;
END //
DELIMITER ;

-- (C) Thêm lần thanh toán -> cộng dồn tổng đã trả (kích hoạt (B))
DELIMITER //
CREATE TRIGGER trg_tt_dongbo_hoadon_ins
AFTER INSERT ON ThanhToan FOR EACH ROW
BEGIN
    UPDATE HoaDon
    SET DaThanhToan = (SELECT COALESCE(SUM(SoTien),0) FROM ThanhToan WHERE MaHD = NEW.MaHD)
    WHERE MaHD = NEW.MaHD;
END //
DELIMITER ;

-- (D) Xóa lần thanh toán -> tính lại
DELIMITER //
CREATE TRIGGER trg_tt_dongbo_hoadon_del
AFTER DELETE ON ThanhToan FOR EACH ROW
BEGIN
    UPDATE HoaDon
    SET DaThanhToan = (SELECT COALESCE(SUM(SoTien),0) FROM ThanhToan WHERE MaHD = OLD.MaHD)
    WHERE MaHD = OLD.MaHD;
END //
DELIMITER ;

-- 3) Cập nhật lại hóa đơn đã có cho khớp logic mới
UPDATE HoaDon SET DaThanhToan = DaThanhToan;
```

---

## VIỆC B — SỬA PHẦN JAVA / GIAO DIỆN

**Prompt:**
> Sau khi đã chạy `07_fix_thanhtoan.sql`, sửa phần hóa đơn trong ứng dụng JavaFX:
>
> 1. **Khi tạo hóa đơn** (trong service tạo hóa đơn, dùng transaction): insert `HoaDon` với `DaThanhToan = 0`, KHÔNG set trạng thái tay. Với số tiền khách trả ngay lúc đó, insert một dòng vào bảng `ThanhToan`. Trigger sẽ tự cập nhật `DaThanhToan`, `ConNo`, `TrangThai`. Sau khi commit, đọc lại hóa đơn để lấy trạng thái mới.
>
> 2. **Cập nhật model `HoaDon`**: thêm trường `ConNo`. Enum trạng thái có 3 giá trị: `CHUA_TT`, `MOT_PHAN`, `DA_TT`.
>
> 3. **Bảng danh sách hóa đơn**: thêm cột **"Còn nợ"** (hiển thị `ConNo`). Cột **Trạng thái** hiển thị 3 mức với màu:
>    - CHUA_TT → "Chưa TT" (màu đỏ)
>    - MOT_PHAN → "Trả một phần" (màu cam)
>    - DA_TT → "Đã TT" (màu xanh)
>
> 4. **Thêm nút "Thanh toán"** ở cột Thao tác, CHỈ bật khi hóa đơn chưa trả đủ (CHUA_TT hoặc MOT_PHAN). Bấm vào mở dialog nhập số tiền trả thêm + hình thức (Tiền mặt/Chuyển khoản/Thẻ), khi lưu thì insert một dòng `ThanhToan` cho hóa đơn đó rồi refresh bảng (trạng thái + còn nợ tự đổi nhờ trigger).
>
> 5. Định dạng tiền dạng 1,200,000.
>
> Làm xong chạy `mvn javafx:run` và test theo kịch bản bên dưới.

---

## KỊCH BẢN KIỂM THỬ
1. Tạo hóa đơn tổng 1.200.000, trả ngay 600.000 → hiển thị **"Trả một phần"**, Còn nợ **600.000**.
2. Bấm **Thanh toán**, trả thêm 600.000 → tự chuyển **"Đã TT"**, Còn nợ **0**.
3. Tạo hóa đơn không trả đồng nào → **"Chưa TT"**, Còn nợ = tổng tiền.
4. Kiểm tra trong DB: `SELECT MaHD, DaThanhToan, ConNo, TrangThai FROM HoaDon;` khớp với giao diện.

## GHI CHÚ
- Toàn bộ logic trạng thái/công nợ nằm ở **trigger trong DB**, nên dù thêm thanh toán từ bất kỳ đâu, số liệu vẫn luôn khớp.
- Đây cũng là nền để sau này làm cột **"Nợ hiện tại"** ở màn Khách hàng (cộng `ConNo` của các hóa đơn theo từng khách) — nếu muốn làm tiếp.
