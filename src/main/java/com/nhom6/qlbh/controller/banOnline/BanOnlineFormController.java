package com.nhom6.qlbh.controller.banOnline;

import com.nhom6.qlbh.dao.KhachHangDAO;
import com.nhom6.qlbh.dao.SanPhamDAO;
import com.nhom6.qlbh.model.*;
import com.nhom6.qlbh.service.AuthService;
import com.nhom6.qlbh.service.BanOnlineService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.util.List;

public class BanOnlineFormController {

    @FXML private Label    lblMaDHO;
    @FXML private ComboBox<NenTangOnline> cboNenTang;
    @FXML private ComboBox<KhachHang>     cboKhachHang;
    @FXML private TextField txtDiaChi;
    @FXML private ComboBox<String>        cboDonVi;
    @FXML private TextField txtMaSP, txtTenSP, txtSoLuong, txtDonGia;
    @FXML private Label     lblTonKho, lblAddError;
    @FXML private TableView<ChiTietHD>    tblChiTiet;
    @FXML private TableColumn<ChiTietHD, String>  ctColMaSP, ctColTenSP, ctColGia, ctColThanh;
    @FXML private TableColumn<ChiTietHD, Integer> ctColSL;
    @FXML private TableColumn<ChiTietHD, Void>    ctColXoa;
    @FXML private Label     lblTongTien;
    @FXML private Button    btnXacNhan;

    private final BanOnlineService service = new BanOnlineService();
    private final SanPhamDAO       spDAO   = new SanPhamDAO();
    private final KhachHangDAO     khDAO   = new KhachHangDAO();
    private final ObservableList<ChiTietHD> chiTiet = FXCollections.observableArrayList();
    private BigDecimal giaVonTemp = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        // Nền tảng
        try {
            cboNenTang.setItems(FXCollections.observableArrayList(service.getNenTangOnlines()));
            cboNenTang.getSelectionModel().selectFirst();
        } catch (Exception e) { /* ignored */ }

        // Khách hàng
        try {
            List<KhachHang> list = khDAO.findAll();
            KhachHang le = new KhachHang(); le.setTenKH("— Khách lẻ / không có TK —");
            list.add(0, le);
            cboKhachHang.setItems(FXCollections.observableArrayList(list));
            cboKhachHang.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(KhachHang v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); return; }
                    setText(v.getMaKH() == null ? "— Khách lẻ —" : v.getMaKH() + " – " + v.getTenKH());
                }
            });
            cboKhachHang.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(KhachHang v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); return; }
                    setText(v.getMaKH() == null ? "— Khách lẻ —" : v.getMaKH() + " – " + v.getTenKH());
                }
            });
            cboKhachHang.getSelectionModel().selectFirst();
        } catch (Exception e) { /* ignored */ }

        // Đơn vị vận chuyển
        cboDonVi.setItems(FXCollections.observableArrayList(
            "Giao Hàng Nhanh (GHN)", "Giao Hàng Tiết Kiệm (GHTK)",
            "Viettel Post", "J&T Express", "Shopee Express", "Lazada Express"));
        cboDonVi.getSelectionModel().selectFirst();

        // Table
        ctColMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        ctColTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        ctColSL.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuong()).asObject());
        ctColGia.setCellValueFactory(c   -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDonGia())));
        ctColThanh.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getThanhTien())));
        ctColXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("×");
            {
                btn.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;-fx-font-weight:bold;-fx-cursor:hand;");
                btn.setOnAction(e -> { chiTiet.remove(getTableView().getItems().get(getIndex())); refreshTotal(); });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : btn);
            }
        });
        tblChiTiet.setItems(chiTiet);

        onNenTangChanged();
    }

    @FXML
    public void onNenTangChanged() {
        NenTangOnline nt = cboNenTang.getValue();
        if (nt == null) return;
        try {
            String maDHO = service.generateMaDHO(nt.getMaNT());
            lblMaDHO.setText(maDHO);
        } catch (Exception ignored) {}
    }

    @FXML public void onTraCuu() {
        lblAddError.setText(""); lblTonKho.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        if (maSP.isEmpty()) { lblAddError.setText("Nhập mã SP."); return; }
        try {
            SanPham sp = spDAO.findById(maSP);
            if (sp == null) { lblAddError.setText("Không tìm thấy SP \"" + maSP + "\"."); return; }
            txtTenSP.setText(sp.getTenSP());
            txtDonGia.setText(FormatUtil.currency(sp.getGiaBan()));
            giaVonTemp = sp.getGiaVon();
            lblTonKho.setText(String.valueOf(sp.getTonKho()));
            txtSoLuong.requestFocus();
        } catch (Exception e) { lblAddError.setText("Lỗi: " + e.getMessage()); }
    }

    @FXML public void onThemSP() {
        lblAddError.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        String tenSP = txtTenSP.getText().trim();
        if (maSP.isEmpty() || tenSP.isEmpty()) { lblAddError.setText("Tra cứu SP trước."); return; }
        int sl;
        try { sl = Integer.parseInt(txtSoLuong.getText().trim()); if (sl <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { lblAddError.setText("Số lượng phải là số nguyên dương."); return; }
        for (ChiTietHD c : chiTiet)
            if (c.getMaSP().equals(maSP)) { lblAddError.setText("SP đã có trong đơn."); return; }

        BigDecimal gia = FormatUtil.parseCurrency(txtDonGia.getText());
        chiTiet.add(new ChiTietHD(maSP, tenSP, sl, gia, giaVonTemp));
        refreshTotal();
        txtMaSP.clear(); txtTenSP.clear(); txtSoLuong.setText("1"); txtDonGia.clear();
        lblTonKho.setText(""); giaVonTemp = BigDecimal.ZERO;
        txtMaSP.requestFocus();
    }

    private void refreshTotal() {
        BigDecimal tong = chiTiet.stream().map(ChiTietHD::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTongTien.setText(FormatUtil.currency(tong) + " đ");
    }

    @FXML public void onXacNhan() {
        try {
            NenTangOnline nt = cboNenTang.getValue();
            if (nt == null) { AlertUtil.warn("Thiếu thông tin", "Vui lòng chọn nền tảng."); return; }
            String diaChi = txtDiaChi.getText().trim();
            if (diaChi.isEmpty()) { AlertUtil.warn("Thiếu địa chỉ", "Vui lòng nhập địa chỉ giao hàng."); return; }
            if (chiTiet.isEmpty()) { AlertUtil.warn("Chưa có SP", "Thêm ít nhất 1 sản phẩm."); return; }

            DonHangOnline don = new DonHangOnline();
            don.setMaDHO(lblMaDHO.getText());
            don.setMaNT(nt.getMaNT());

            HoaDon hd = new HoaDon();
            hd.setMaHD(service.generateMaHD());
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null && kh.getMaKH() != null) hd.setMaKH(kh.getMaKH());
            var user = AuthService.getCurrentUser();
            if (user != null && user.getNhanVien() != null) hd.setMaNV(user.getNhanVien().getMaNV());
            hd.setGiamGia(BigDecimal.ZERO);

            String donVi = cboDonVi.getValue() != null ? cboDonVi.getValue() : "Giao Hàng Nhanh (GHN)";
            service.taoDoHang(don, hd, chiTiet, diaChi, donVi);

            AlertUtil.info("Đơn hàng thành công",
                "Đơn " + nt.getTenNT() + " [" + don.getMaDHO() + "] đã xử lý!\n\n" +
                "• Hóa đơn tạo: " + hd.getMaHD() + "\n" +
                "• Vận đơn: " + don.getMaVD() + "\n" +
                "• Đơn vị VC: " + donVi + "\n\n" +
                "★ Tồn kho đã được cập nhật tự động qua trigger MySQL.");
            goBack();
        } catch (Exception e) { AlertUtil.error("Lỗi tạo đơn", e.getMessage()); }
    }

    @FXML public void onHuy() { goBack(); }

    private void goBack() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/banOnline/ban-online.fxml"));
            ((StackPane) btnXacNhan.getScene().getRoot().lookup("#contentArea")).getChildren().setAll(view);
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }
}
