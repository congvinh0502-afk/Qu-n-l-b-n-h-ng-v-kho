package com.nhom6.qlbh.controller.hanghoa;

import com.nhom6.qlbh.dao.LoaiSanPhamDAO;
import com.nhom6.qlbh.model.LoaiSanPham;
import com.nhom6.qlbh.model.SanPham;
import com.nhom6.qlbh.service.SanPhamService;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class SanPhamDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMaSP;
    @FXML private TextField txtTenSP;
    @FXML private ComboBox<LoaiSanPham> cboLoai;
    @FXML private TextField txtGiaVon;
    @FXML private TextField txtGiaBan;
    @FXML private ComboBox<String> cboTrangThai;
    @FXML private Label lblError;
    @FXML private Button btnLuu;

    private final SanPhamService service = new SanPhamService();
    private final LoaiSanPhamDAO loaiDAO = new LoaiSanPhamDAO();

    private SanPham current;
    private boolean isNew;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Load categories
        try {
            List<LoaiSanPham> list = loaiDAO.findAll();
            list.add(0, new LoaiSanPham(0, "— Chọn loại —"));
            cboLoai.setItems(FXCollections.observableArrayList(list));
            cboLoai.getSelectionModel().selectFirst();
        } catch (Exception e) {
            lblError.setText("Lỗi tải danh mục: " + e.getMessage());
        }

        cboTrangThai.setItems(FXCollections.observableArrayList("Đang kinh doanh", "Ngừng kinh doanh"));
        cboTrangThai.getSelectionModel().selectFirst();

        // Format currency fields on focus lost
        txtGiaVon.focusedProperty().addListener((obs, o, n) -> {
            if (!n) formatCurrencyField(txtGiaVon);
        });
        txtGiaBan.focusedProperty().addListener((obs, o, n) -> {
            if (!n) formatCurrencyField(txtGiaBan);
        });
    }

    public void setSanPham(SanPham sp, boolean isNew) {
        this.isNew = isNew;
        this.current = sp;
        if (isNew) {
            lblTitle.setText("Thêm sản phẩm mới");
        } else {
            lblTitle.setText("Sửa sản phẩm");
            txtMaSP.setText(sp.getMaSP());
            txtMaSP.setEditable(false);
            txtMaSP.setStyle("-fx-background-color: #f1f5f9;");
            txtTenSP.setText(sp.getTenSP());
            txtGiaVon.setText(FormatUtil.currency(sp.getGiaVon()));
            txtGiaBan.setText(FormatUtil.currency(sp.getGiaBan()));
            cboTrangThai.getSelectionModel().select(sp.getTrangThai() == 1 ? 0 : 1);

            if (sp.getMaLoai() != null) {
                cboLoai.getItems().stream()
                    .filter(l -> l.getMaLoai() == sp.getMaLoai())
                    .findFirst()
                    .ifPresent(cboLoai.getSelectionModel()::select);
            }
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void onLuu() {
        lblError.setText("");
        try {
            SanPham sp = new SanPham();
            sp.setMaSP(txtMaSP.getText().trim().toUpperCase());
            sp.setTenSP(txtTenSP.getText().trim());

            LoaiSanPham loai = cboLoai.getValue();
            sp.setMaLoai((loai == null || loai.getMaLoai() == 0) ? null : loai.getMaLoai());

            sp.setGiaVon(FormatUtil.parseCurrency(txtGiaVon.getText()));
            sp.setGiaBan(FormatUtil.parseCurrency(txtGiaBan.getText()));
            sp.setTrangThai(cboTrangThai.getSelectionModel().getSelectedIndex() == 0 ? 1 : 0);

            if (isNew) service.them(sp);
            else service.sua(sp);

            saved = true;
            close();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() { close(); }

    private void close() {
        ((Stage) btnLuu.getScene().getWindow()).close();
    }

    private void formatCurrencyField(TextField tf) {
        String text = tf.getText();
        if (!text.isBlank()) {
            tf.setText(FormatUtil.currency(FormatUtil.parseCurrency(text)));
        }
    }
}
