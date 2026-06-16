package com.nhom6.qlbh.controller.khachhang;

import com.nhom6.qlbh.model.KhachHang;
import com.nhom6.qlbh.service.KhachHangService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class KhachHangDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMaKH;
    @FXML private TextField txtTenKH;
    @FXML private TextField txtDienThoai;
    @FXML private Label lblError;
    @FXML private Button btnLuu;

    private final KhachHangService service = new KhachHangService();
    private boolean isNew;
    private boolean saved = false;

    public void setKhachHang(KhachHang kh, boolean isNew) {
        this.isNew = isNew;
        if (isNew) {
            lblTitle.setText("Thêm khách hàng mới");
        } else {
            lblTitle.setText("Sửa khách hàng");
            txtMaKH.setText(kh.getMaKH());
            txtMaKH.setEditable(false);
            txtMaKH.setStyle("-fx-background-color: #f1f5f9;");
            txtTenKH.setText(kh.getTenKH());
            txtDienThoai.setText(kh.getDienThoai());
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void onLuu() {
        lblError.setText("");
        try {
            KhachHang kh = new KhachHang();
            kh.setMaKH(txtMaKH.getText().trim().toUpperCase());
            kh.setTenKH(txtTenKH.getText().trim());
            kh.setDienThoai(txtDienThoai.getText().trim());

            if (isNew) service.them(kh);
            else service.sua(kh);

            saved = true;
            close();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML private void onCancel() { close(); }

    private void close() { ((Stage) btnLuu.getScene().getWindow()).close(); }
}
