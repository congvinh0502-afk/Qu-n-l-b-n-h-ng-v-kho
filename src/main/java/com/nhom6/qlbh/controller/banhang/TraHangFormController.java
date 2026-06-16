package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.dao.HoaDonDAO;
import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.ChiTietTH;
import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.model.TraHang;
import com.nhom6.qlbh.service.AuthService;
import com.nhom6.qlbh.service.TraHangService;
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
import java.util.ArrayList;
import java.util.List;

public class TraHangFormController {

    @FXML private Label lblMaTra;
    @FXML private TextField txtMaHD;
    @FXML private Label lblHDInfo;
    @FXML private TableView<ChiTietTH> tblTraHang;
    @FXML private TableColumn<ChiTietTH, String>  trColMaSP, trColTenSP, trColDonGia, trColThanh;
    @FXML private TableColumn<ChiTietTH, Integer> trColSLGoc, trColSLTra;
    @FXML private Label lblCanTra;
    @FXML private TextField txtDaTraKhach, txtLyDo;
    @FXML private Button btnLuu;

    private final TraHangService service = new TraHangService();
    private final HoaDonDAO      hdDAO   = new HoaDonDAO();
    private HoaDon currentHoaDon;
    private final ObservableList<ChiTietTH> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        trColMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        trColTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        trColSLGoc.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuongGoc()).asObject());
        trColSLTra.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSoLuong()).asObject());
        trColDonGia.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDonGia())));
        trColThanh.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getThanhTien())));

        // Make SL trả column editable via spinner-like TextField
        trColSLTra.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(70);
                tf.textProperty().addListener((obs, o, n) -> {
                    if (getIndex() < 0 || getIndex() >= rows.size()) return;
                    try {
                        int val = n.isBlank() ? 0 : Integer.parseInt(n);
                        ChiTietTH ct = rows.get(getIndex());
                        ct.setSoLuong(Math.max(0, Math.min(val, ct.getSoLuongGoc())));
                        refreshTotal();
                    } catch (NumberFormatException ignored) {}
                });
            }
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                tf.setText(v != null ? String.valueOf(v) : "0");
                setGraphic(tf);
            }
        });

        tblTraHang.setItems(rows);

        try { lblMaTra.setText("Mã phiếu: " + service.generateMaTra()); }
        catch (Exception e) { lblMaTra.setText(""); }
    }

    /** Called from HoaDonController when navigating directly to this form */
    public void preloadHoaDon(String maHD) {
        txtMaHD.setText(maHD);
        onNapHoaDon();
    }

    @FXML
    public void onNapHoaDon() {
        String maHD = txtMaHD.getText().trim().toUpperCase();
        if (maHD.isEmpty()) { lblHDInfo.setText("Nhập mã hóa đơn."); return; }
        try {
            HoaDon hd = hdDAO.findById(maHD);
            if (hd == null) { lblHDInfo.setText("Không tìm thấy hóa đơn \"" + maHD + "\".");  return; }
            currentHoaDon = hd;
            lblHDInfo.setText("Khách: " + hd.getTenKH() + "  |  Tổng: " + FormatUtil.currency(hd.getTongSauGiamGia()) + " đ");

            List<ChiTietHD> goc = service.findChiTietHoaDon(maHD);
            rows.clear();
            for (ChiTietHD g : goc) {
                ChiTietTH ct = new ChiTietTH(g.getMaSP(), g.getTenSP(), 0, g.getDonGia());
                ct.setSoLuongGoc(g.getSoLuong());
                rows.add(ct);
            }
            refreshTotal();
        } catch (Exception e) { lblHDInfo.setText("Lỗi: " + e.getMessage()); }
    }

    private void refreshTotal() {
        BigDecimal tong = rows.stream().map(ChiTietTH::getThanhTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblCanTra.setText(FormatUtil.currency(tong) + " đ");
        txtDaTraKhach.setText(FormatUtil.currency(tong));
    }

    @FXML
    public void onLuu() {
        try {
            List<ChiTietTH> selected = new ArrayList<>();
            for (ChiTietTH ct : rows)
                if (ct.getSoLuong() > 0) selected.add(ct);

            if (selected.isEmpty()) { AlertUtil.warn("Chưa chọn", "Nhập số lượng trả > 0 cho ít nhất 1 sản phẩm."); return; }

            TraHang th = new TraHang();
            th.setMaTra(service.generateMaTra());
            if (currentHoaDon != null) {
                th.setMaHD(currentHoaDon.getMaHD());
                th.setMaKH(currentHoaDon.getMaKH());
            }
            var user = AuthService.getCurrentUser();
            if (user != null && user.getNhanVien() != null) th.setMaNV(user.getNhanVien().getMaNV());
            th.setLyDo(txtLyDo.getText().trim());
            th.setDaTraKhach(FormatUtil.parseCurrency(txtDaTraKhach.getText()));
            th.setChiTiet(selected);

            service.tao(th);
            AlertUtil.info("Thành công", "Đã lưu phiếu trả " + th.getMaTra());
            goBack("/fxml/banhang/tra-hang.fxml");
        } catch (Exception e) { AlertUtil.error("Lỗi lưu phiếu trả", e.getMessage()); }
    }

    @FXML public void onHuy() { goBack("/fxml/banhang/tra-hang.fxml"); }

    private void goBack(String path) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(path));
            ((StackPane) btnLuu.getScene().getRoot().lookup("#contentArea")).getChildren().setAll(view);
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }
}
