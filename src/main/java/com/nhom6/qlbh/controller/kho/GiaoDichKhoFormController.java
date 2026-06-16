package com.nhom6.qlbh.controller.kho;

import com.nhom6.qlbh.dao.NhaCungCapDAO;
import com.nhom6.qlbh.dao.SanPhamDAO;
import com.nhom6.qlbh.model.ChiTietGDK;
import com.nhom6.qlbh.model.GiaoDichKho;
import com.nhom6.qlbh.model.NhaCungCap;
import com.nhom6.qlbh.model.SanPham;
import com.nhom6.qlbh.service.AuthService;
import com.nhom6.qlbh.service.GiaoDichKhoService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class GiaoDichKhoFormController {

    @FXML private VBox formRoot;
    @FXML private Label lblMaGD;
    @FXML private ComboBox<String> cboLoaiGD;
    @FXML private HBox rowNCC;
    @FXML private ComboBox<NhaCungCap> cboNCC;
    @FXML private TextField txtGhiChu;
    @FXML private TextField txtMaSP;
    @FXML private TextField txtTenSP;
    @FXML private TextField txtSoLuong;
    @FXML private Label lblDonGiaLabel;
    @FXML private TextField txtDonGia;
    @FXML private Label lblAddError;
    @FXML private Label lblTonKho;
    @FXML private TableView<ChiTietGDK> tblChiTiet;
    @FXML private TableColumn<ChiTietGDK, String> ctColMaSP;
    @FXML private TableColumn<ChiTietGDK, String> ctColTenSP;
    @FXML private TableColumn<ChiTietGDK, Integer> ctColSoLuong;
    @FXML private TableColumn<ChiTietGDK, String> ctColDonGia;
    @FXML private TableColumn<ChiTietGDK, String> ctColThanh;
    @FXML private TableColumn<ChiTietGDK, Void> ctColXoa;
    @FXML private Label lblTong;
    @FXML private Button btnLuu;

    private final GiaoDichKhoService service = new GiaoDichKhoService();
    private final SanPhamDAO spDAO = new SanPhamDAO();
    private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();
    private final ObservableList<ChiTietGDK> chiTiet = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Loại GD
        cboLoaiGD.setItems(FXCollections.observableArrayList("NHAP", "TRA_NHAP", "KIEM_KHO", "XUAT_HUY"));
        cboLoaiGD.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); setText(empty || v == null ? null : labelOf(v));
            }
        });
        cboLoaiGD.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); setText(empty || v == null ? null : labelOf(v));
            }
        });
        cboLoaiGD.getSelectionModel().selectFirst();

        // NCC
        try {
            List<NhaCungCap> list = nccDAO.findAll();
            cboNCC.setItems(FXCollections.observableArrayList(list));
            cboNCC.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(NhaCungCap v, boolean empty) {
                    super.updateItem(v, empty); setText(empty || v == null ? null : v.getMaNCC() + " – " + v.getTenNCC());
                }
            });
            cboNCC.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(NhaCungCap v, boolean empty) {
                    super.updateItem(v, empty); setText(empty || v == null ? null : v.getMaNCC() + " – " + v.getTenNCC());
                }
            });
        } catch (Exception e) {
            lblAddError.setText("Lỗi tải NCC: " + e.getMessage());
        }

        // Table
        ctColMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        ctColTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        ctColSoLuong.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSoLuong()).asObject());
        ctColDonGia.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDonGiaNhap())));
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

        // Default state
        onLoaiGDChange();
        refreshMaGD();
    }

    @FXML
    public void onLoaiGDChange() {
        String loai = cboLoaiGD.getValue();
        boolean needNCC = "NHAP".equals(loai) || "TRA_NHAP".equals(loai);
        rowNCC.setVisible(needNCC); rowNCC.setManaged(needNCC);

        boolean showPrice = "NHAP".equals(loai) || "TRA_NHAP".equals(loai);
        lblDonGiaLabel.setVisible(showPrice); lblDonGiaLabel.setManaged(showPrice);
        txtDonGia.setVisible(showPrice); txtDonGia.setManaged(showPrice);

        if ("KIEM_KHO".equals(loai)) {
            txtSoLuong.setPromptText("Lệch (+/-)");
        } else {
            txtSoLuong.setPromptText("Số lượng");
        }
        refreshMaGD();
    }

    private void refreshMaGD() {
        try {
            String loai = cboLoaiGD.getValue();
            if (loai != null) {
                String ma = service.generateMaGD(loai);
                lblMaGD.setText("Mã phiếu: " + ma);
            }
        } catch (Exception e) {
            lblMaGD.setText("");
        }
    }

    @FXML
    public void onTraCuu() {
        lblAddError.setText("");
        lblTonKho.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        if (maSP.isEmpty()) { lblAddError.setText("Nhập mã SP."); return; }
        try {
            SanPham sp = spDAO.findById(maSP);
            if (sp == null) {
                txtTenSP.setText(""); lblAddError.setText("Không tìm thấy sản phẩm \"" + maSP + "\".");
            } else {
                txtTenSP.setText(sp.getTenSP());
                lblTonKho.setText("Tồn kho hiện tại: " + sp.getTonKho());
                if ("NHAP".equals(cboLoaiGD.getValue()))
                    txtDonGia.setText(FormatUtil.currency(sp.getGiaVon()));
                txtSoLuong.requestFocus();
            }
        } catch (Exception e) {
            lblAddError.setText("Lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void onThemSP() {
        lblAddError.setText("");
        String maSP = txtMaSP.getText().trim().toUpperCase();
        String tenSP = txtTenSP.getText().trim();
        if (maSP.isEmpty()) { lblAddError.setText("Nhập mã SP trước."); return; }
        if (tenSP.isEmpty()) { lblAddError.setText("Tra cứu sản phẩm trước."); return; }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException e) {
            lblAddError.setText("Số lượng phải là số nguyên."); return;
        }

        // Check duplicate
        for (ChiTietGDK c : chiTiet) {
            if (c.getMaSP().equals(maSP)) { lblAddError.setText("Sản phẩm \"" + maSP + "\" đã có trong danh sách."); return; }
        }

        String loai = cboLoaiGD.getValue();
        BigDecimal donGia = BigDecimal.ZERO;
        if ("NHAP".equals(loai) || "TRA_NHAP".equals(loai)) {
            donGia = FormatUtil.parseCurrency(txtDonGia.getText());
        }

        chiTiet.add(new ChiTietGDK(maSP, tenSP, soLuong, donGia));
        refreshTotal();

        // Clear inputs
        txtMaSP.clear(); txtTenSP.clear(); txtSoLuong.clear(); txtDonGia.clear();
        lblTonKho.setText("");
        txtMaSP.requestFocus();
    }

    private void refreshTotal() {
        BigDecimal total = chiTiet.stream()
            .map(ChiTietGDK::getThanhTien)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTong.setText("Tổng tiền: " + FormatUtil.currency(total) + " đ");
    }

    @FXML
    public void onLuu() {
        try {
            GiaoDichKho gdk = new GiaoDichKho();
            gdk.setLoaiGD(cboLoaiGD.getValue());
            gdk.setMaGD(service.generateMaGD(gdk.getLoaiGD()));

            NhaCungCap ncc = cboNCC.getValue();
            gdk.setMaNCC(ncc != null ? ncc.getMaNCC() : null);

            var user = AuthService.getCurrentUser();
            if (user != null && user.getNhanVien() != null)
                gdk.setMaNV(user.getNhanVien().getMaNV());

            gdk.setGhiChu(txtGhiChu.getText().trim());
            gdk.setChiTiet(chiTiet);

            service.luu(gdk);
            AlertUtil.info("Thành công", "Đã lưu phiếu " + gdk.getMaGD());
            goBack();
        } catch (Exception e) {
            AlertUtil.error("Lỗi lưu phiếu", e.getMessage());
        }
    }

    @FXML
    public void onHuy() { goBack(); }

    private void goBack() {
        try {
            Parent list = FXMLLoader.load(getClass().getResource("/fxml/kho/giao-dich-kho.fxml"));
            StackPane contentArea = (StackPane) btnLuu.getScene().getRoot().lookup("#contentArea");
            contentArea.getChildren().setAll(list);
        } catch (Exception e) {
            AlertUtil.error("Lỗi", e.getMessage());
        }
    }

    private static String labelOf(String loai) {
        switch (loai) {
            case "NHAP":     return "Nhập hàng";
            case "TRA_NHAP": return "Trả hàng nhập";
            case "KIEM_KHO": return "Kiểm kho";
            case "XUAT_HUY": return "Xuất hủy";
            default: return loai;
        }
    }
}
