package com.nhom6.qlbh.controller;

import com.nhom6.qlbh.model.TaiKhoan;
import com.nhom6.qlbh.model.VaiTro;
import com.nhom6.qlbh.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;
    @FXML private Label     lblWelcome;
    @FXML private StackPane contentArea;

    // Menu buttons
    @FXML private Button btnHangHoa;
    @FXML private Button btnKhachHang;
    @FXML private Button btnNhaCungCap;
    @FXML private Button btnKho;
    @FXML private Button btnBanHang;
    @FXML private Button btnTraHang;
    @FXML private Button btnPhanTich;
    @FXML private Button btnBanOnline;

    private Button activeBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TaiKhoan user = AuthService.getCurrentUser();
        if (user != null) {
            lblUserName.setText(user.getTenNV());
            lblUserRole.setText(user.getVaiTro().getLabel());
            lblWelcome.setText("Xin chào, " + user.getTenNV() + "! Chọn chức năng từ menu bên trái.");
            applyRoleAccess(user.getVaiTro());
        }
    }

    /** Ẩn menu không thuộc quyền của vai trò */
    private void applyRoleAccess(VaiTro vaiTro) {
        if (vaiTro == VaiTro.QUANLY) return; // QUANLY thấy tất cả

        if (vaiTro == VaiTro.BANHANG) {
            btnKho.setVisible(false);       btnKho.setManaged(false);
            btnNhaCungCap.setVisible(false); btnNhaCungCap.setManaged(false);
            btnPhanTich.setVisible(false);  btnPhanTich.setManaged(false);
            btnBanOnline.setVisible(false); btnBanOnline.setManaged(false);
        } else if (vaiTro == VaiTro.KHO) {
            btnBanHang.setVisible(false);   btnBanHang.setManaged(false);
            btnTraHang.setVisible(false);   btnTraHang.setManaged(false);
            btnKhachHang.setVisible(false); btnKhachHang.setManaged(false);
            btnPhanTich.setVisible(false);  btnPhanTich.setManaged(false);
            btnBanOnline.setVisible(false); btnBanOnline.setManaged(false);
        } else if (vaiTro == VaiTro.CSKH) {
            btnKho.setVisible(false);       btnKho.setManaged(false);
            btnHangHoa.setVisible(false);   btnHangHoa.setManaged(false);
            btnNhaCungCap.setVisible(false); btnNhaCungCap.setManaged(false);
            btnBanHang.setVisible(false);   btnBanHang.setManaged(false);
            btnTraHang.setVisible(false);   btnTraHang.setManaged(false);
            btnPhanTich.setVisible(false);  btnPhanTich.setManaged(false);
            btnBanOnline.setVisible(false); btnBanOnline.setManaged(false);
        }
    }

    @FXML
    private void onMenu(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        setActiveButton(clicked);

        if (clicked == btnHangHoa) {
            loadView("/fxml/hanghoa/san-pham.fxml");
        } else if (clicked == btnKhachHang) {
            loadView("/fxml/khachhang/khach-hang.fxml");
        } else if (clicked == btnNhaCungCap) {
            loadView("/fxml/nhacc/nha-cung-cap.fxml");
        } else if (clicked == btnKho) {
            loadView("/fxml/kho/giao-dich-kho.fxml");
        } else if (clicked == btnBanHang) {
            loadView("/fxml/banhang/hoa-don.fxml");
        } else if (clicked == btnTraHang) {
            loadView("/fxml/banhang/tra-hang.fxml");
        } else if (clicked == btnPhanTich) {
            loadView("/fxml/phanTich/phan-tich.fxml");
        } else if (clicked == btnBanOnline) {
            loadView("/fxml/banOnline/ban-online.fxml");
        } else {
            showPlaceholder(clicked.getText().trim());
        }
    }

    private void setActiveButton(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("menu-btn-active");
        }
        activeBtn = btn;
        if (!btn.getStyleClass().contains("menu-btn-active")) {
            btn.getStyleClass().add("menu-btn-active");
        }
    }

    /** Hiển thị placeholder cho các module chưa làm — sẽ thay bằng FXML thật */
    private void showPlaceholder(String moduleName) {
        Label lbl = new Label("Module: " + moduleName + "\n(Đang phát triển)");
        lbl.setStyle("-fx-font-size:18px; -fx-text-fill:#9e9e9e; -fx-alignment:CENTER;");
        lbl.setWrapText(true);
        contentArea.getChildren().setAll(lbl);
    }

    /** Load một FXML vào khu vực nội dung — dùng ở các giai đoạn sau */
    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogout() {
        AuthService.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 550));
            stage.setMaximized(false);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
