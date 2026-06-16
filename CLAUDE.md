# CLAUDE.md — Hướng dẫn cho Claude Code

> File này Claude Code TỰ ĐỌC mỗi phiên. Mục tiêu: nắm ngữ cảnh project + quy ước + thứ tự công việc, để không phải giải thích lại mỗi lần.

## 1. Project là gì
Phần mềm **Quản lý Bán hàng & Kho** (đồ án môn CSDL), bản desktop.
Bản thiết kế chi tiết nằm ở file **`ROADMAP.md`** (đọc file đó trước khi làm bất cứ việc gì).

## 2. Công nghệ BẮT BUỘC dùng
- Java 17, JavaFX 17 (FXML + SceneBuilder), JavaFX CSS.
- MySQL 8, kết nối qua JDBC + HikariCP.
- Maven (build), maven-shade-plugin (đóng gói .jar).
- BCrypt (jbcrypt) cho mật khẩu, Apache POI cho xuất Excel.
- Không tự ý đổi sang Spring Boot, Hibernate hay web — đây là đồ án JavaFX thuần + JDBC để thể hiện kiến thức SQL.

## 3. Kiến trúc 3 tầng — TUÂN THỦ NGHIÊM
```
controller (FXML) → service (nghiệp vụ) → dao (JDBC) → MySQL
```
Cấu trúc thư mục:
```
src/main/java/com/nhom6/qlbh/{config,model,dao,service,controller,util}
src/main/resources/{fxml,css,images}
db/{01_schema.sql,02_triggers.sql,03_views.sql,04_indexes.sql,05_sample_data.sql}
```
Controller KHÔNG được viết SQL trực tiếp — luôn gọi qua service → dao.

## 4. Quy ước code
- Đặt tên bảng/cột tiếng Việt không dấu theo ROADMAP (MaSP, TenSP, HoaDon...).
- DAO luôn dùng `PreparedStatement` (cấm nối chuỗi SQL — chống SQL Injection).
- Mọi nghiệp vụ ghi nhiều bảng (tạo hóa đơn, trả hàng) phải bọc trong **transaction** (`setAutoCommit(false)` + `commit/rollback`).
- Bắt `SQLException` từ trigger (vd "Khong du ton kho") và hiển thị `Alert` thân thiện lên UI.
- Encoding UTF-8 mọi nơi; JDBC URL có `useUnicode=true&characterEncoding=utf8`; DB dùng `utf8mb4`.
- Tiền hiển thị bằng `NumberFormat` dạng `1,400,000`.
- Tồn kho do **TRIGGER** trong DB tự cập nhật — code Java KHÔNG tự cộng/trừ TonKho, chỉ ghi chi tiết giao dịch.

## 5. Lưu ý thiết kế DB (đã chốt — đừng làm sai)
- Quan hệ **HoaDon ↔ TraHang chỉ đi 1 CHIỀU**: `TraHang.MaHD → HoaDon`. KHÔNG thêm `MaTraHang` làm FK trong HoaDon (tránh chu trình khóa ngoại). Cột "Mã trả hàng" lấy bằng JOIN.
- Bảng chi tiết dùng khóa chính kép (vd `ChiTietHoaDon` PK = MaHD + MaSP).
- `ChiTietHoaDon` lưu thêm `GiaVon` (chốt tại thời điểm bán) để tính lợi nhuận gộp.

## 6. THỨ TỰ LÀM VIỆC (làm xong từng bước mới sang bước sau, luôn chạy thử)
1. Tạo project Maven JavaFX chạy được cửa sổ trống (Giai đoạn 1 trong ROADMAP).
2. Viết `db/01_schema.sql` — tạo đủ 18 bảng + khóa ngoại (Phần 3 ROADMAP).
3. Viết `02_triggers.sql`, `03_views.sql`, `04_indexes.sql` (Phần 4) + `05_sample_data.sql`.
4. `DBConnection` + Đăng nhập + phân quyền + màn hình chính (Giai đoạn 3).
5. Module Hàng hóa (CRUD mẫu hoàn chỉnh) → rồi nhân bản cho Khách hàng, Nhà cung cấp.
6. Module Kho (Nhập hàng, Trả hàng nhập, Kiểm kho, Xuất hủy).
7. Module Bán hàng (Đặt hàng → Hóa đơn → Thanh toán → Trả hàng) — phần lõi, dùng transaction.
8. Module Phân tích/Báo cáo (BarChart/LineChart từ các VIEW) + nút Xuất Excel.
9. Bán online (mô phỏng) + kiểm thử + đóng gói .jar.

## 7. Cách làm hiệu quả
- Khi tạo module CRUD đầu tiên (Sản phẩm), làm **đầy đủ 1 module mẫu**: model + dao + service + controller + fxml. Các module sau nhân theo mẫu này.
- Sau mỗi giai đoạn: chạy `mvn javafx:run` test, báo cáo kết quả, hỏi trước khi sang giai đoạn tiếp.
- Khi sửa schema, cập nhật luôn file SQL trong `db/` cho khớp.

## 8. Yêu cầu môi trường (kiểm tra trước khi chạy)
- JDK 17, Maven, MySQL 8 đã cài và chạy.
- Tạo sẵn database rỗng tên `qlbh` (hoặc nhắc người dùng tạo).
- Nếu thiếu công cụ, hướng dẫn người dùng cài từng bước.
