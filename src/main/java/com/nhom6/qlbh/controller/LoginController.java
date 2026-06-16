package com.nhom6.qlbh.controller;

import com.nhom6.qlbh.model.TaiKhoan;
import com.nhom6.qlbh.service.AuthService;
import com.nhom6.qlbh.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnLogin;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        lblError.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        try {
            TaiKhoan tk = authService.login(username, password);
            if (tk == null) {
                lblError.setText("Sai tên đăng nhập hoặc mật khẩu.");
                txtPassword.clear();
                return;
            }
            switchToMain();
        } catch (Exception e) {
            AlertUtil.error("Lỗi kết nối", "Không thể kết nối cơ sở dữ liệu.\n" + e.getMessage());
        }
    }

    private void switchToMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
    }
}
