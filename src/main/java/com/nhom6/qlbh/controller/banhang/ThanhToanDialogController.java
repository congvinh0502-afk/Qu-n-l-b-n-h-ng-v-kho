package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.service.ThanhToanService;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ThanhToanDialogController {

    @FXML private Label lblInfo, lblConLai, lblError;
    @FXML private TextField txtSoTien, txtTienKhach;
    @FXML private Label lblTienThoi;
    @FXML private ComboBox<String> cboHinhThuc;
    @FXML private HBox rowTienKhach;
    @FXML private Button btnLuu;

    private final ThanhToanService service = new ThanhToanService();
    private HoaDon hoaDon;
    private boolean saved = false;

    @FXML
    public void initialize() {
        cboHinhThuc.setItems(FXCollections.observableArrayList("TIEN_MAT", "CHUYEN_KHOAN", "THE"));
        cboHinhThuc.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                switch (v) { case "TIEN_MAT": setText("Tiền mặt"); break;
                             case "CHUYEN_KHOAN": setText("Chuyển khoản"); break;
                             case "THE": setText("Thẻ"); break; default: setText(v); }
            }
        });
        cboHinhThuc.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                switch (v) { case "TIEN_MAT": setText("Tiền mặt"); break;
                             case "CHUYEN_KHOAN": setText("Chuyển khoản"); break;
                             case "THE": setText("Thẻ"); break; default: setText(v); }
            }
        });
        cboHinhThuc.getSelectionModel().selectFirst();
    }

    public void setHoaDon(HoaDon hd) {
        this.hoaDon = hd;
        lblInfo.setText("HĐ: " + hd.getMaHD() + "  |  KH: " + hd.getTenKH() +
                        "\nTổng: " + FormatUtil.currency(hd.getTongSauGiamGia()) + " đ" +
                        "  |  Đã TT: " + FormatUtil.currency(hd.getDaThanhToan()) + " đ");
        lblConLai.setText(FormatUtil.currency(hd.getConLai()) + " đ");
        txtSoTien.setText(FormatUtil.currency(hd.getConLai()));
    }

    public boolean isSaved() { return saved; }

    @FXML public void onSoTienChanged() {
        if ("TIEN_MAT".equals(cboHinhThuc.getValue())) updateTienThoi();
    }

    @FXML public void onHinhThucChanged() {
        boolean isCash = "TIEN_MAT".equals(cboHinhThuc.getValue());
        rowTienKhach.setVisible(isCash); rowTienKhach.setManaged(isCash);
        if (isCash) updateTienThoi();
    }

    @FXML public void onTienKhachChanged() { updateTienThoi(); }

    private void updateTienThoi() {
        BigDecimal soTien = FormatUtil.parseCurrency(txtSoTien.getText());
        BigDecimal tienKhach = txtTienKhach.getText().isBlank() ? soTien : FormatUtil.parseCurrency(txtTienKhach.getText());
        BigDecimal thoi = tienKhach.subtract(soTien);
        lblTienThoi.setText(FormatUtil.currency(thoi.max(BigDecimal.ZERO)) + " đ");
    }

    @FXML public void onLuu() {
        lblError.setText("");
        try {
            BigDecimal soTien = FormatUtil.parseCurrency(txtSoTien.getText());
            String hinhThuc = cboHinhThuc.getValue();
            service.thanhToan(hoaDon, soTien, hinhThuc);
            saved = true;
            close();
        } catch (Exception e) { lblError.setText(e.getMessage()); }
    }

    @FXML public void onCancel() { close(); }

    private void close() { ((Stage) btnLuu.getScene().getWindow()).close(); }
}
