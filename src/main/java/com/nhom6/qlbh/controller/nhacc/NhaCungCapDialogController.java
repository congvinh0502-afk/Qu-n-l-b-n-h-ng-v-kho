package com.nhom6.qlbh.controller.nhacc;

import com.nhom6.qlbh.model.NhaCungCap;
import com.nhom6.qlbh.service.NhaCungCapService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NhaCungCapDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMaNCC;
    @FXML private TextField txtTenNCC;
    @FXML private TextField txtDienThoai;
    @FXML private TextField txtDiaChi;
    @FXML private Label lblError;
    @FXML private Button btnLuu;

    private final NhaCungCapService service = new NhaCungCapService();
    private boolean isNew;
    private boolean saved = false;

    public void setNhaCungCap(NhaCungCap ncc, boolean isNew) {
        this.isNew = isNew;
        if (isNew) {
            lblTitle.setText("Thêm nhà cung cấp mới");
        } else {
            lblTitle.setText("Sửa nhà cung cấp");
            txtMaNCC.setText(ncc.getMaNCC());
            txtMaNCC.setEditable(false);
            txtMaNCC.setStyle("-fx-background-color: #f1f5f9;");
            txtTenNCC.setText(ncc.getTenNCC());
            txtDienThoai.setText(ncc.getDienThoai());
            txtDiaChi.setText(ncc.getDiaChi());
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void onLuu() {
        lblError.setText("");
        try {
            NhaCungCap ncc = new NhaCungCap();
            ncc.setMaNCC(txtMaNCC.getText().trim().toUpperCase());
            ncc.setTenNCC(txtTenNCC.getText().trim());
            ncc.setDienThoai(txtDienThoai.getText().trim());
            ncc.setDiaChi(txtDiaChi.getText().trim());

            if (isNew) service.them(ncc);
            else service.sua(ncc);

            saved = true;
            close();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML private void onCancel() { close(); }

    private void close() { ((Stage) btnLuu.getScene().getWindow()).close(); }
}
