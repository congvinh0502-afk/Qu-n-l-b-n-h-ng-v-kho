package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.dao.KhachHangDAO;
import com.nhom6.qlbh.dao.SanPhamDAO;
import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.model.KhachHang;
import com.nhom6.qlbh.model.SanPham;
import com.nhom6.qlbh.service.AuthService;
import com.nhom6.qlbh.service.HoaDonService;
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

public class HoaDonFormController {

    @FXML private Label lblMaHD;
    @FXML private ComboBox<KhachHang> cboKhachHang;
    @FXML private TextField txtMaSP, txtTenSP, txtSoLuong, txtDonGia;
    @FXML private Label lblAddError, lblTonKho;
    @FXML private TableView<ChiTietHD> tblChiTiet;
    @FXML private TableColumn<ChiTietHD, String> ctColMaSP, ctColTenSP, ctColDonGia, ctColThanh;
    @FXML private TableColumn<ChiTietHD, Integer> ctColSL;
    @FXML private TableColumn<ChiTietHD, Void> ctColXoa;
    @FXML private Label lblTongTien, lblSauGiam, lblConLai;
    @FXML private TextField txtGiamGia, txtTrNgay;
    @FXML private ComboBox<String> cboHinhThuc;
    @FXML private Button btnLuu;

    private final HoaDonService service   = new HoaDonService();
    private final SanPhamDAO    spDAO     = new SanPhamDAO();
    private final KhachHangDAO  khDAO     = new KhachHangDAO();
    private final ObservableList<ChiTietHD> chiTiet = FXCollections.observableArrayList();
    private BigDecimal giaVonTemp = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        // Load customers
        try {
            List<KhachHang> list = khDAO.findAll();
            list.add(0, new KhachHang()); list.get(0).setTenKH("— Khách lẻ —");
            cboKhachHang.setItems(FXCollections.observableArrayList(list));
            cboKhachHang.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(KhachHang v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : (v.getMaKH() == null ? "— Khách lẻ —" : v.getMaKH() + " – " + v.getTenKH()));
                }
            });
            cboKhachHang.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(KhachHang v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : (v.getMaKH() == null ? "— Khách lẻ —" : v.getMaKH() + " – " + v.getTenKH()));
                }
            });
            cboKhachHang.getSelectionModel().selectFirst();
        } catch (Exception e) { lblAddError.setText("Lỗi tải KH: " + e.getMessage()); }

        // Payment method options
        cboHinhThuc.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "Thẻ"));
        cboHinhThuc.getSelectionModel().selectFirst();

        // Table setup
        ctColMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        ctColTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        ctColSL.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuong()).asObject());
        ctColDonGia.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDonGia())));
        ctColThanh.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getThanhTien())));
        ctColXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("×");
            { btn.setStyle("-fx-background-color:#ffebee;-fx-text-fill:#c62828;-fx-font-weight:bold;-fx-cursor:hand;");
              btn.setOnAction(e -> { chiTiet.remove(getTableView().getItems().get(getIndex())); refreshTotal(); }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : btn);
            }
        });
        tblChiTiet.setItems(chiTiet);

        // Generate ID
        try { lblMaHD.setText("Mã HĐ: " + service.generateMaHD()); }
        catch (Exception e) { lblMaHD.setText(""); }
    }

    @FXML public void onTraCuu() {
        lblAddError.setText(""); lblTonKho.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        if (maSP.isEmpty()) { lblAddError.setText("Nhập mã SP."); return; }
        try {
            SanPham sp = spDAO.findById(maSP);
            if (sp == null) { txtTenSP.setText(""); lblAddError.setText("Không tìm thấy \"" + maSP + "\".");
            } else {
                txtTenSP.setText(sp.getTenSP());
                txtDonGia.setText(FormatUtil.currency(sp.getGiaBan()));
                giaVonTemp = sp.getGiaVon();
                lblTonKho.setText("Tồn kho: " + sp.getTonKho());
                txtSoLuong.requestFocus();
            }
        } catch (Exception e) { lblAddError.setText("Lỗi: " + e.getMessage()); }
    }

    @FXML public void onThemSP() {
        lblAddError.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        String tenSP = txtTenSP.getText().trim();
        if (maSP.isEmpty() || tenSP.isEmpty()) { lblAddError.setText("Tra cứu sản phẩm trước."); return; }
        int sl;
        try { sl = Integer.parseInt(txtSoLuong.getText().trim()); if (sl <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { lblAddError.setText("Số lượng phải là số nguyên dương."); return; }
        for (ChiTietHD c : chiTiet)
            if (c.getMaSP().equals(maSP)) { lblAddError.setText("SP \"" + maSP + "\" đã có trong danh sách."); return; }

        BigDecimal donGia = FormatUtil.parseCurrency(txtDonGia.getText());
        chiTiet.add(new ChiTietHD(maSP, tenSP, sl, donGia, giaVonTemp));
        refreshTotal();
        txtMaSP.clear(); txtTenSP.clear(); txtSoLuong.clear(); txtDonGia.clear();
        lblTonKho.setText(""); giaVonTemp = BigDecimal.ZERO;
        txtMaSP.requestFocus();
    }

    @FXML public void onGiamGiaChanged() { refreshTotal(); }
    @FXML public void onTrNgayChanged()  { refreshTotal(); }

    private void refreshTotal() {
        BigDecimal tong = chiTiet.stream().map(ChiTietHD::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTongTien.setText(FormatUtil.currency(tong) + " đ");
        BigDecimal giam = FormatUtil.parseCurrency(txtGiamGia.getText());
        BigDecimal sau  = tong.subtract(giam.min(tong));
        lblSauGiam.setText(FormatUtil.currency(sau) + " đ");
        BigDecimal trNgay = FormatUtil.parseCurrency(txtTrNgay.getText());
        BigDecimal conLai = sau.subtract(trNgay.min(sau));
        lblConLai.setText(FormatUtil.currency(conLai) + " đ");
    }

    @FXML public void onLuu() {
        try {
            HoaDon hd = new HoaDon();
            hd.setMaHD(service.generateMaHD());
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null && kh.getMaKH() != null) { hd.setMaKH(kh.getMaKH()); }
            var user = AuthService.getCurrentUser();
            if (user != null && user.getNhanVien() != null) hd.setMaNV(user.getNhanVien().getMaNV());
            hd.setGiamGia(FormatUtil.parseCurrency(txtGiamGia.getText()));
            hd.setChiTiet(chiTiet);

            BigDecimal trNgay = FormatUtil.parseCurrency(txtTrNgay.getText());
            String hinhThuc   = cboHinhThuc.getValue();
            service.tao(hd, trNgay, hinhThuc);
            AlertUtil.info("Thành công", "Đã tạo hóa đơn " + hd.getMaHD());
            goBack("/fxml/banhang/hoa-don.fxml");
        } catch (Exception e) { AlertUtil.error("Lỗi tạo hóa đơn", e.getMessage()); }
    }

    @FXML public void onHuy() { goBack("/fxml/banhang/hoa-don.fxml"); }

    private void goBack(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            StackPane ca = (StackPane) btnLuu.getScene().getRoot().lookup("#contentArea");
            ca.getChildren().setAll(view);
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }
}
