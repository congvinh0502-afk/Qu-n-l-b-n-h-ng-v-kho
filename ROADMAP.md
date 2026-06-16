# LỘ TRÌNH PHÁT TRIỂN PHẦN MỀM "QUẢN LÝ BÁN HÀNG & KHO"
### JavaFX (Desktop) + Cơ sở dữ liệu quan hệ (Trigger / View / Index / Stored Procedure)

> Nhóm 6 — Trịnh Công Vinh, Nguyễn Khánh Toàn, Bùi Tuấn Đạt
> Tài liệu này nối tiếp Proposal và bản thiết kế hệ thống đã có, bổ sung phần còn thiếu để biến mô tả nghiệp vụ thành một phần mềm chạy được.

---

## PHẦN 0 — ĐÁNH GIÁ HIỆN TRẠNG (nhóm đang ở đâu)

Đã có:
- Bối cảnh, mục tiêu, đối tượng sử dụng rõ ràng.
- Danh sách 18 thực thể và mô tả nghiệp vụ từng module (Hàng hóa, Đơn hàng, Khách hàng, Phân tích, Bán online).
- Bộ ảnh KiotViet thật → đây là "đặc tả giao diện" rất giá trị, cho biết chính xác cột dữ liệu cần có.

Còn thiếu (đây là việc cần làm tiếp):
1. **Schema chi tiết**: 18 bảng mới chỉ có tên + ý nghĩa, chưa có cột, kiểu dữ liệu, khóa chính/khóa ngoại.
2. **Sơ đồ ERD thực sự** (quan hệ 1-n, n-n).
3. **Logic nghiệp vụ tự động**: trigger trừ/cộng tồn kho, cập nhật tổng bán khách hàng.
4. **View báo cáo** phục vụ các màn hình Phân tích trong ảnh.
5. **Kiến trúc ứng dụng JavaFX** (chia tầng) + cách kết nối DB qua JDBC.
6. **Dữ liệu mẫu** và **kế hoạch kiểm thử**.

Tài liệu này lấp đúng 6 khoảng trống đó.

---

## PHẦN 1 — CÔNG NGHỆ ĐỀ XUẤT

| Thành phần | Lựa chọn đề xuất | Lý do |
|---|---|---|
| Ngôn ngữ | Java 17 (LTS) | Ổn định, JavaFX hỗ trợ tốt |
| Giao diện | JavaFX 17 + FXML + SceneBuilder | Tách UI khỏi code, kéo-thả nhanh |
| CSS | JavaFX CSS | Làm giao diện xanh giống KiotViet |
| CSDL | **MySQL 8** (hoặc SQL Server nếu môn học yêu cầu) | Miễn phí, hỗ trợ trigger/view/index/procedure đầy đủ |
| Kết nối | JDBC + **HikariCP** (connection pool) | Chuẩn, hiệu năng tốt |
| Build | **Maven** | Quản lý thư viện, dễ chấm/đóng gói |
| Biểu đồ | JavaFX `BarChart`, `LineChart` | Dựng đúng các màn hình "Phân tích" |
| Mã hóa mật khẩu | BCrypt (jbcrypt) | Bảng Tài khoản an toàn |
| (Tùy chọn) Xuất Excel | Apache POI | Nút "Xuất file" trong ảnh |

> **Lưu ý chọn CSDL:** nếu giảng viên dạy SQL Server thì dùng SQL Server (cú pháp trigger `INSERTED`/`DELETED`). Tài liệu này viết theo **MySQL** vì miễn phí và phổ biến cho JavaFX. Mình ghi chú điểm khác biệt khi cần.

---

## PHẦN 2 — KIẾN TRÚC PHẦN MỀM (chia tầng)

Áp dụng mô hình **3 tầng + MVC** để code sạch, dễ chấm, dễ chia việc 3 người:

```
┌─────────────────────────────────────────────┐
│  TẦNG GIAO DIỆN (View - FXML + Controller)    │  ← JavaFX
│  ProductView, InvoiceView, CustomerView ...   │
└───────────────────┬───────────────────────────┘
                    │ gọi
┌───────────────────▼───────────────────────────┐
│  TẦNG NGHIỆP VỤ (Service)                       │  ← Java thuần
│  Kiểm tra ràng buộc, tính tổng tiền, validate   │
└───────────────────┬───────────────────────────┘
                    │ gọi
┌───────────────────▼───────────────────────────┐
│  TẦNG TRUY CẬP DỮ LIỆU (DAO/Repository)         │  ← JDBC
│  Câu lệnh SQL: SELECT/INSERT/UPDATE/DELETE      │
└───────────────────┬───────────────────────────┘
                    │ JDBC
┌───────────────────▼───────────────────────────┐
│  CƠ SỞ DỮ LIỆU (MySQL)                          │
│  Bảng + Trigger + View + Index + Procedure      │
└─────────────────────────────────────────────────┘
```

Cấu trúc thư mục Maven gợi ý:
```
src/main/java/com/nhom6/qlbh/
├── App.java                  (điểm khởi động)
├── config/DBConnection.java  (HikariCP)
├── model/                    (Product, Customer, Invoice...)
├── dao/                      (ProductDAO, InvoiceDAO...)
├── service/                  (ProductService...)
├── controller/               (ProductController...)
└── util/                     (Alert, Format tiền...)
src/main/resources/
├── fxml/                     (product.fxml, invoice.fxml...)
├── css/style.css
└── images/
db/
├── 01_schema.sql
├── 02_triggers.sql
├── 03_views.sql
├── 04_indexes.sql
└── 05_sample_data.sql
```

---

## PHẦN 3 — THIẾT KẾ CƠ SỞ DỮ LIỆU CHI TIẾT (phần quan trọng nhất)

Dưới đây là schema đầy đủ cho 18 thực thể, suy ra từ tài liệu nhóm + các trường nhìn thấy trong ảnh KiotViet.

### 3.1 Nhóm danh mục

**LoaiSanPham (Category)**
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaLoai | INT PK AUTO_INCREMENT | |
| TenLoai | VARCHAR(100) NOT NULL | |

**NhaCungCap (Supplier)** — ảnh 1, 9
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaNCC | VARCHAR(20) PK | VD: NCC002, NCC17 |
| TenNCC | VARCHAR(150) NOT NULL | |
| DienThoai | VARCHAR(20) | |
| DiaChi | VARCHAR(255) | |

**SanPham (Product)** — ảnh 3
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaSP | VARCHAR(30) PK | VD: SP85133153 |
| TenSP | VARCHAR(255) NOT NULL | |
| MaLoai | INT FK → LoaiSanPham | |
| GiaVon | DECIMAL(15,2) DEFAULT 0 | giá nhập (để tính lợi nhuận gộp) |
| GiaBan | DECIMAL(15,2) DEFAULT 0 | |
| TonKho | INT DEFAULT 0 | cập nhật tự động bằng trigger |
| TrangThai | TINYINT DEFAULT 1 | 1=đang KD, 0=ngừng |

**KhachHang (Customer)** — ảnh 5
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaKH | VARCHAR(20) PK | VD: KH001242 |
| TenKH | VARCHAR(150) NOT NULL | |
| DienThoai | VARCHAR(30) | |
| TongBan | DECIMAL(18,2) DEFAULT 0 | tự động cập nhật |
| TongBanTruTra | DECIMAL(18,2) DEFAULT 0 | tự động cập nhật |

### 3.2 Nhóm người dùng hệ thống

**NhanVien (Employee)**
| Cột | Kiểu |
|---|---|
| MaNV | INT PK AUTO_INCREMENT |
| TenNV | VARCHAR(150) NOT NULL |
| DienThoai | VARCHAR(20) |
| ChucVu | VARCHAR(50) |

**TaiKhoan (Account)**
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaTK | INT PK AUTO_INCREMENT | |
| TenDangNhap | VARCHAR(50) UNIQUE NOT NULL | |
| MatKhau | VARCHAR(255) NOT NULL | lưu hash BCrypt |
| MaNV | INT FK → NhanVien | |
| VaiTro | ENUM('QUANLY','BANHANG','KHO','CSKH') | phân quyền |

### 3.3 Nhóm đặt hàng

**DonDatHang (PurchaseOrder/SalesOrder)** — ảnh 12 (DH053717)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaDH | VARCHAR(20) PK | VD: DH053717 |
| ThoiGian | DATETIME NOT NULL | |
| MaKH | VARCHAR(20) FK → KhachHang | |
| MaNV | INT FK → NhanVien | |
| TongTienHang | DECIMAL(18,2) | |
| GiamGia | DECIMAL(18,2) DEFAULT 0 | |
| TongSauGiamGia | DECIMAL(18,2) | |
| TrangThai | ENUM('PHIEU_TAM','HOAN_THANH') | "Phiếu tạm"/"Hoàn thành" trong ảnh |

**ChiTietDonDatHang (OrderDetail)**
| Cột | Kiểu |
|---|---|
| MaDH | VARCHAR(20) FK → DonDatHang |
| MaSP | VARCHAR(30) FK → SanPham |
| SoLuong | INT NOT NULL |
| DonGia | DECIMAL(15,2) |
| (PK kép: MaDH + MaSP) | |

### 3.4 Nhóm hóa đơn & thanh toán

**HoaDon (Invoice)** — ảnh 2 (HD056448)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaHD | VARCHAR(20) PK | |
| ThoiGian | DATETIME NOT NULL | |
| MaKH | VARCHAR(20) FK → KhachHang | |
| MaNV | INT FK → NhanVien | |
| TongTienHang | DECIMAL(18,2) | |
| GiamGia | DECIMAL(18,2) DEFAULT 0 | |
| TongSauGiamGia | DECIMAL(18,2) | |
| TrangThai | ENUM('DA_TT','CHUA_TT') | đã/chưa thanh toán |
| DaThanhToan | DECIMAL(18,2) DEFAULT 0 | |

> ⚠️ **KHÔNG đặt cột `MaTraHang` (FK → TraHang) trong HoaDon.** Cột "Mã trả hàng" hiển thị trong ảnh KiotViet chỉ là kết quả JOIN khi xem (lấy từ `TraHang.MaHD`), KHÔNG phải khóa ngoại lưu trong bảng. Nếu đặt FK 2 chiều HoaDon↔TraHang sẽ tạo **chu trình khóa ngoại** (xem mục 3.9). Quan hệ chỉ đi 1 chiều: `TraHang.MaHD → HoaDon`.

**ChiTietHoaDon (InvoiceDetail)** — bảng quan trọng để trigger trừ kho
| Cột | Kiểu |
|---|---|
| MaHD | VARCHAR(20) FK → HoaDon |
| MaSP | VARCHAR(30) FK → SanPham |
| SoLuong | INT NOT NULL |
| DonGia | DECIMAL(15,2) |
| GiaVon | DECIMAL(15,2) | chốt giá vốn để tính lợi nhuận gộp |
| (PK kép: MaHD + MaSP) | |

**ThanhToan (Payment)**
| Cột | Kiểu |
|---|---|
| MaTT | INT PK AUTO_INCREMENT |
| MaHD | VARCHAR(20) FK → HoaDon |
| SoTien | DECIMAL(18,2) |
| HinhThuc | ENUM('TIEN_MAT','CHUYEN_KHOAN','THE') |
| ThoiGian | DATETIME |

### 3.5 Nhóm trả hàng (khách trả)

**TraHang (Return)** — ảnh 15 (TH002971)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaTra | VARCHAR(20) PK | |
| MaHD | VARCHAR(20) FK → HoaDon NULL | trả nhanh có thể null |
| MaKH | VARCHAR(20) FK → KhachHang | |
| MaNV | INT FK → NhanVien | "Người bán" |
| ThoiGian | DATETIME | |
| TongTienHang | DECIMAL(18,2) | |
| CanTraKhach | DECIMAL(18,2) | |
| DaTraKhach | DECIMAL(18,2) DEFAULT 0 | |
| LyDo | VARCHAR(255) | sai hàng / muốn trả |
| TrangThai | ENUM('DA_TRA','DA_HUY') | |

**ChiTietTraHang (ReturnDetail)**
| Cột | Kiểu |
|---|---|
| MaTra | VARCHAR(20) FK → TraHang |
| MaSP | VARCHAR(30) FK → SanPham |
| SoLuong | INT |
| DonGia | DECIMAL(15,2) |
| (PK kép) | |

### 3.6 Nhóm kho

**GiaoDichKho (StockTransaction)** — gom Nhập hàng (ảnh 1), Trả hàng nhập (ảnh 9), Kiểm kho (ảnh 14), Xuất hủy (ảnh 16)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaGD | VARCHAR(20) PK | PN016170 / THN000261 / KK018583 |
| LoaiGD | ENUM('NHAP','TRA_NHAP','KIEM_KHO','XUAT_HUY') | |
| MaNCC | VARCHAR(20) FK → NhaCungCap NULL | với nhập/trả nhập |
| MaNV | INT FK → NhanVien | |
| ThoiGian | DATETIME | |
| GhiChu | VARCHAR(255) | cột "Ghi chú" trong kiểm kho |
| TrangThai | VARCHAR(30) | |

**ChiTietGiaoDichKho (StockTransactionDetail)**
| Cột | Kiểu | Ghi chú |
|---|---|---|
| MaGD | VARCHAR(20) FK | |
| MaSP | VARCHAR(30) FK | |
| SoLuong | INT | với kiểm kho: SL lệch (±) |
| DonGiaNhap | DECIMAL(15,2) | |
| (PK kép) | |

### 3.7 Nhóm mở rộng

**YeuCauSuaChua (RepairRequest)** — ảnh 8 menu
| Cột | Kiểu |
|---|---|
| MaYC | INT PK AUTO_INCREMENT |
| MaKH | VARCHAR(20) FK |
| MaSP | VARCHAR(30) FK |
| TinhTrang | VARCHAR(255) |
| TrangThai | ENUM('TIEP_NHAN','DANG_SUA','HOAN_THANH','HUY') |
| ThoiGian | DATETIME |

**VanDon (Shipment)** — ảnh 8
| Cột | Kiểu |
|---|---|
| MaVD | VARCHAR(20) PK |
| MaHD | VARCHAR(20) FK → HoaDon |
| DiaChiGiao | VARCHAR(255) |
| DonViVanChuyen | VARCHAR(100) |
| TrangThaiGiao | ENUM('CHO_LAY','DANG_GIAO','DA_GIAO','HOAN') |

**NenTangOnline (Platform)** — ảnh 11
| Cột | Kiểu |
|---|---|
| MaNT | INT PK AUTO_INCREMENT |
| TenNT | VARCHAR(50) | Shopee, Lazada, Tiki, Tiktok... |

**DonHangOnline (OnlineOrder)**
| Cột | Kiểu |
|---|---|
| MaDHO | VARCHAR(30) PK |
| MaNT | INT FK → NenTangOnline |
| MaHD | VARCHAR(20) FK → HoaDon NULL |
| ThoiGian | DATETIME |
| TrangThai | VARCHAR(30) |

### 3.8 Quan hệ chính (ERD tóm tắt)

```
LoaiSanPham 1──n SanPham n──1 (qua chi tiết) ──> nhiều bảng giao dịch
NhaCungCap  1──n GiaoDichKho 1──n ChiTietGiaoDichKho n──1 SanPham
KhachHang   1──n DonDatHang  1──n ChiTietDonDatHang  n──1 SanPham
KhachHang   1──n HoaDon      1──n ChiTietHoaDon      n──1 SanPham
HoaDon      1──n ThanhToan
HoaDon      1──n TraHang     1──n ChiTietTraHang     n──1 SanPham
HoaDon      1──1 VanDon
NhanVien    1──n TaiKhoan / HoaDon / DonDatHang
NenTangOnline 1──n DonHangOnline
```

### 3.9 ⚠️ Tránh CHU TRÌNH KHÓA NGOẠI (điểm giáo viên nhắc)

**Vấn đề:** nếu thiết kế cả hai chiều
```
HoaDon.MaTraHang ──FK──> TraHang        (hóa đơn trỏ tới phiếu trả)
TraHang.MaHD     ──FK──> HoaDon         (phiếu trả trỏ về hóa đơn)
```
thì tạo thành **chu trình**: `HoaDon → TraHang → HoaDon`. Hậu quả:
- Không INSERT được cái nào trước (con–gà–quả–trứng), buộc phải để FK NULL rồi UPDATE sau → dễ sai dữ liệu.
- Khó xóa, khó tạo bảng (MySQL/SQL Server có thể báo lỗi thứ tự tạo bảng), vi phạm chuẩn hóa.

**Cách sửa (đã áp dụng trong schema này):** chỉ giữ **một chiều** —
```
TraHang.MaHD ──FK──> HoaDon     ✅  (1 hóa đơn có thể có nhiều phiếu trả: 1──n)
```
Bỏ hẳn `MaTraHang` khỏi `HoaDon`. Muốn hiển thị "Mã trả hàng" như ảnh KiotViet thì **JOIN khi xem**, không lưu FK ngược:
```sql
SELECT hd.MaHD, hd.ThoiGian, t.MaTra AS MaTraHang
FROM HoaDon hd
LEFT JOIN TraHang t ON t.MaHD = hd.MaHD;
```

**Kiểm tra nhanh cả 18 bảng — sau khi sửa, mọi quan hệ đều đi 1 chiều, KHÔNG còn vòng:**
- Các bảng "chi tiết" (ChiTietHoaDon, ChiTietDonDatHang, ChiTietTraHang, ChiTietGiaoDichKho) đều chỉ trỏ **lên** bảng cha + trỏ sang `SanPham`, không có bảng nào trỏ ngược lại chúng → **không tạo chu trình** (đây là dạng quan hệ n–n được tách đúng chuẩn, an toàn).
- `TaiKhoan → NhanVien`, `DonHangOnline → HoaDon`, `VanDon → HoaDon`, `ThanhToan → HoaDon`... đều 1 chiều.
- Đồ thị khóa ngoại sau khi sửa là **đồ thị có hướng không chu trình (DAG)** → đạt yêu cầu.

> Mẹo trình bày với giáo viên: vẽ ERD và chỉ rõ "đã phát hiện nguy cơ chu trình HoaDon↔TraHang và xử lý bằng quan hệ 1 chiều + JOIN hiển thị". Đây đúng là điều giáo viên muốn thấy.

---

## PHẦN 4 — VẬN DỤNG KIẾN THỨC CSDL (Trigger / View / Index / Procedure)

Đây là phần ghi điểm với môn CSDL. Cú pháp viết theo MySQL 8.

### 4.1 TRIGGER — tự động hóa nghiệp vụ

**(1) Bán hàng → tự trừ tồn kho** (khi thêm dòng chi tiết hóa đơn)
```sql
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
```

**(2) Nhập hàng → tự cộng tồn kho** (chi tiết giao dịch kho loại NHAP)
```sql
DELIMITER //
CREATE TRIGGER trg_nhaphang_congtonkho
AFTER INSERT ON ChiTietGiaoDichKho
FOR EACH ROW
BEGIN
    DECLARE loai VARCHAR(20);
    SELECT LoaiGD INTO loai FROM GiaoDichKho WHERE MaGD = NEW.MaGD;
    IF loai = 'NHAP' THEN
        UPDATE SanPham SET TonKho = TonKho + NEW.SoLuong WHERE MaSP = NEW.MaSP;
    ELSEIF loai IN ('TRA_NHAP','XUAT_HUY') THEN
        UPDATE SanPham SET TonKho = TonKho - NEW.SoLuong WHERE MaSP = NEW.MaSP;
    ELSEIF loai = 'KIEM_KHO' THEN
        UPDATE SanPham SET TonKho = TonKho + NEW.SoLuong WHERE MaSP = NEW.MaSP; -- SL lệch ±
    END IF;
END //
DELIMITER ;
```

**(3) Khách trả hàng → cộng lại tồn kho**
```sql
DELIMITER //
CREATE TRIGGER trg_trahang_conghangtonkho
AFTER INSERT ON ChiTietTraHang
FOR EACH ROW
BEGIN
    UPDATE SanPham SET TonKho = TonKho + NEW.SoLuong WHERE MaSP = NEW.MaSP;
END //
DELIMITER ;
```

**(4) Tự cập nhật "Tổng bán" của khách khi có hóa đơn**
```sql
DELIMITER //
CREATE TRIGGER trg_capnhat_tongban
AFTER INSERT ON HoaDon
FOR EACH ROW
BEGIN
    UPDATE KhachHang
    SET TongBan = TongBan + NEW.TongSauGiamGia,
        TongBanTruTra = TongBanTruTra + NEW.TongSauGiamGia
    WHERE MaKH = NEW.MaKH;
END //
DELIMITER ;
```

**(5) Khách trả hàng → giảm "Tổng bán trừ trả hàng"**
```sql
DELIMITER //
CREATE TRIGGER trg_trahang_giamtongban
AFTER INSERT ON TraHang
FOR EACH ROW
BEGIN
    UPDATE KhachHang
    SET TongBanTruTra = TongBanTruTra - NEW.TongTienHang
    WHERE MaKH = NEW.MaKH;
END //
DELIMITER ;
```

**(6) Chặn bán quá tồn kho** (ràng buộc nghiệp vụ)
```sql
DELIMITER //
CREATE TRIGGER trg_check_tonkho
BEFORE INSERT ON ChiTietHoaDon
FOR EACH ROW
BEGIN
    DECLARE ton INT;
    SELECT TonKho INTO ton FROM SanPham WHERE MaSP = NEW.MaSP;
    IF ton < NEW.SoLuong THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Khong du ton kho de ban!';
    END IF;
END //
DELIMITER ;
```

### 4.2 VIEW — phục vụ các màn hình Phân tích trong ảnh

**(A) Doanh thu thuần theo ngày/tuần** (ảnh 4)
```sql
CREATE VIEW v_doanhthu_ngay AS
SELECT DATE(ThoiGian) AS Ngay,
       SUM(TongSauGiamGia) AS DoanhThuThuan
FROM HoaDon
GROUP BY DATE(ThoiGian);
```

**(B) Tổng quan hàng hóa & lợi nhuận gộp/SP** (ảnh 6)
```sql
CREATE VIEW v_phantich_sanpham AS
SELECT sp.MaSP, sp.TenSP,
       SUM(ct.SoLuong)                              AS SoLuongBan,
       SUM(ct.SoLuong * ct.DonGia)                  AS DoanhThu,
       SUM(ct.SoLuong * (ct.DonGia - ct.GiaVon))    AS LoiNhuanGop
FROM ChiTietHoaDon ct
JOIN SanPham sp ON sp.MaSP = ct.MaSP
GROUP BY sp.MaSP, sp.TenSP;
```

**(C) Top sản phẩm bán chạy** (ảnh 7) — dùng `ORDER BY ... LIMIT 10` trong câu truy vấn từ view (B).

**(D) Top sản phẩm được đặt nhiều nhất** (ảnh 13)
```sql
CREATE VIEW v_top_dathang AS
SELECT sp.MaSP, sp.TenSP, SUM(ct.SoLuong) AS TongDat
FROM ChiTietDonDatHang ct
JOIN SanPham sp ON sp.MaSP = ct.MaSP
GROUP BY sp.MaSP, sp.TenSP;
```

### 4.3 INDEX — tối ưu tìm kiếm (các ô "Theo mã, tên hàng" trong ảnh)
```sql
CREATE INDEX idx_sp_ten      ON SanPham(TenSP);
CREATE INDEX idx_kh_ten      ON KhachHang(TenKH);
CREATE INDEX idx_kh_sdt      ON KhachHang(DienThoai);
CREATE INDEX idx_hd_thoigian ON HoaDon(ThoiGian);
CREATE INDEX idx_cthd_sp     ON ChiTietHoaDon(MaSP);
```

### 4.4 STORED PROCEDURE — đóng gói nghiệp vụ tạo hóa đơn
```sql
DELIMITER //
CREATE PROCEDURE sp_tao_hoadon(
    IN p_MaHD VARCHAR(20), IN p_MaKH VARCHAR(20), IN p_MaNV INT)
BEGIN
    INSERT INTO HoaDon(MaHD, ThoiGian, MaKH, MaNV, TongTienHang,
                       GiamGia, TongSauGiamGia, TrangThai, DaThanhToan)
    VALUES (p_MaHD, NOW(), p_MaKH, p_MaNV, 0, 0, 0, 'CHUA_TT', 0);
END //
DELIMITER ;
```
> Sau khi thêm các dòng `ChiTietHoaDon`, dùng thêm 1 procedure tính lại tổng tiền (hoặc trigger tổng hợp) cập nhật `TongTienHang`, `TongSauGiamGia`.

---

## PHẦN 5 — KẾT NỐI JAVAFX VỚI CSDL (mẫu code)

**DBConnection (HikariCP):**
```java
public class DBConnection {
    private static final HikariDataSource ds;
    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mysql://localhost:3306/qlbh?useUnicode=true&characterEncoding=utf8");
        cfg.setUsername("root");
        cfg.setPassword("your_password");
        cfg.setMaximumPoolSize(10);
        ds = new HikariDataSource(cfg);
    }
    public static Connection get() throws SQLException { return ds.getConnection(); }
}
```

**DAO mẫu (lấy danh sách sản phẩm cho TableView):**
```java
public List<Product> findAll() {
    String sql = "SELECT MaSP, TenSP, GiaBan, TonKho FROM SanPham";
    List<Product> list = new ArrayList<>();
    try (Connection c = DBConnection.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            list.add(new Product(rs.getString("MaSP"), rs.getString("TenSP"),
                                 rs.getBigDecimal("GiaBan"), rs.getInt("TonKho")));
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return list;
}
```
> **Quan trọng:** luôn dùng `PreparedStatement` (chống SQL Injection), luôn dùng transaction (`commit/rollback`) khi tạo hóa đơn nhiều bảng, và bắt `SQLException` từ trigger số (6) để hiện cảnh báo "Không đủ tồn kho" lên giao diện.

---

## PHẦN 6 — LỘ TRÌNH THỰC HIỆN THEO GIAI ĐOẠN

Chia thành 7 giai đoạn (ước lượng ~7–9 tuần cho nhóm 3 người, làm song song được).

### Giai đoạn 1 — Hoàn thiện thiết kế & môi trường (Tuần 1)
- [ ] Chốt schema 18 bảng (Phần 3) → vẽ ERD bằng dbdiagram.io hoặc MySQL Workbench.
- [ ] Cài MySQL, JDK 17, IntelliJ, SceneBuilder, tạo project Maven trống chạy được "Hello JavaFX".
- [ ] Viết `01_schema.sql` tạo toàn bộ bảng + khóa ngoại.
- **Sản phẩm:** ERD + DB rỗng đã tạo bảng.

### Giai đoạn 2 — Tầng CSDL "thông minh" (Tuần 2)
- [ ] Viết `02_triggers.sql`, `03_views.sql`, `04_indexes.sql` (Phần 4).
- [ ] Viết `05_sample_data.sql` nhập vài chục dòng mẫu (lấy số từ ảnh: SP, KH, NCC...).
- [ ] Test trigger bằng tay trong Workbench (INSERT thử → kiểm TonKho thay đổi đúng).
- **Sản phẩm:** DB tự động trừ/cộng kho khi chạy SQL.

### Giai đoạn 3 — Khung ứng dụng + Đăng nhập (Tuần 3)
- [ ] Dựng `DBConnection`, kiến trúc thư mục model/dao/service/controller.
- [ ] Màn hình **Đăng nhập** (bảng TaiKhoan + BCrypt) + phân quyền theo VaiTro.
- [ ] Màn hình chính (menu trái giống KiotViet: Hàng hóa, Đơn hàng, Khách hàng, Phân tích, Bán online).
- **Sản phẩm:** đăng nhập được, điều hướng giữa các màn hình.

### Giai đoạn 4 — Module Hàng hóa & Khách hàng (Tuần 4) — CRUD cơ bản
- [ ] Quản lý Sản phẩm: TableView + thêm/sửa/xóa + tìm kiếm (ảnh 3).
- [ ] Quản lý Khách hàng (ảnh 5), Nhà cung cấp, Loại SP.
- [ ] Module Kho: Nhập hàng (ảnh 1), Trả hàng nhập (ảnh 9), Kiểm kho (ảnh 14), Xuất hủy → ghi GiaoDichKho và **để trigger tự cập tồn**.
- **Sản phẩm:** quản lý được hàng hóa + kho, tồn kho tự đổi.

### Giai đoạn 5 — Module Bán hàng (Tuần 5–6) — phần lõi
- [ ] **Đặt hàng** (ảnh 12): tạo phiếu tạm → chuyển thành hóa đơn.
- [ ] **Hóa đơn** (ảnh 2): màn bán hàng, chọn SP, tính tổng tiền, lưu transaction nhiều bảng.
- [ ] **Thanh toán** + cập nhật trạng thái.
- [ ] **Trả hàng** (ảnh 15): chọn hóa đơn, trả SP → trigger cộng kho + giảm tổng bán.
- [ ] **Vận đơn** + **Yêu cầu sửa chữa** (ảnh 8).
- **Sản phẩm:** chạy được trọn vòng đời: đặt → bán → thanh toán → trả.

### Giai đoạn 6 — Module Phân tích / Báo cáo (Tuần 7)
- [ ] Dùng các VIEW (Phần 4.2) đổ vào BarChart/LineChart.
- [ ] Tổng quan hàng hóa (ảnh 6), Top 10 doanh thu/bán chạy (ảnh 7), Top đặt nhiều (ảnh 13), Doanh thu tuần (ảnh 4).
- [ ] Nút **Xuất file** Excel bằng Apache POI.
- **Sản phẩm:** dashboard có biểu đồ thật từ dữ liệu.

### Giai đoạn 7 — Bán online (mô phỏng) + Kiểm thử + Đóng gói (Tuần 8–9)
- [ ] Module Bán online (ảnh 11): mô phỏng thêm DonHangOnline từ Shopee/Lazada → sinh HoaDon → trigger tự trừ kho (chứng minh "đồng bộ tồn kho").
- [ ] Kiểm thử: nhập dữ liệu mẫu, test 10–15 ca nghiệp vụ (bán quá kho phải báo lỗi, trả hàng cộng kho đúng...).
- [ ] Viết tài liệu + đóng gói file `.jar` (maven-shade-plugin) + script SQL.
- **Sản phẩm:** phần mềm hoàn chỉnh, demo được, có báo cáo.

---

## PHẦN 7 — GỢI Ý CHIA VIỆC 3 NGƯỜI

| Thành viên | Phụ trách chính |
|---|---|
| Người 1 | CSDL (schema, trigger, view, index) + Module Kho/Hàng hóa |
| Người 2 | Module Bán hàng (Đặt hàng, Hóa đơn, Trả hàng) + transaction |
| Người 3 | Đăng nhập/Phân quyền + Phân tích/Báo cáo + Bán online + đóng gói |

Dùng **Git/GitHub** chung repo, mỗi người 1 nhánh, merge cuối tuần.

---

## PHẦN 8 — MỘT SỐ LƯU Ý KỸ THUẬT QUAN TRỌNG

1. **Transaction khi tạo hóa đơn**: ghi `HoaDon` + nhiều `ChiTietHoaDon` phải trong 1 transaction; lỗi giữa chừng phải `rollback` để tránh trừ kho sai.
2. **Encoding tiếng Việt**: DB để `utf8mb4`, JDBC URL thêm `characterEncoding=utf8`, FXML/CSS lưu UTF-8.
3. **Tách logic khỏi trigger khi cần**: nếu giảng viên muốn thấy xử lý ở tầng Java, có thể làm song song (trigger cho toàn vẹn dữ liệu, service kiểm tra trước cho trải nghiệm tốt).
4. **Định dạng tiền**: dùng `NumberFormat` hiển thị `1,400,000` như trong ảnh.
5. **Phát sinh mã**: SP/KH/HD... nên sinh tự động theo tiền tố + số tăng dần (VD `HD` + zero-pad).
6. **Nếu dùng SQL Server thay MySQL**: trigger dùng bảng ảo `INSERTED`/`DELETED` thay cho `NEW`, `AUTO_INCREMENT` → `IDENTITY`, `LIMIT` → `TOP`.

---

## TÓM TẮT 1 DÒNG
Nhóm đã có nghiệp vụ + 18 thực thể → việc còn lại là **(1)** đóng cứng schema chi tiết ở Phần 3, **(2)** cài trigger/view/index ở Phần 4, **(3)** dựng JavaFX 3 tầng ở Phần 2 & 5, rồi **(4)** làm theo 7 giai đoạn ở Phần 6.
