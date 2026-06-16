package com.nhom6.qlbh;

import com.nhom6.qlbh.config.DBConnection;
import com.nhom6.qlbh.util.AlertUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Kiểm tra kết nối DB ngay khi khởi động
            DBConnection.get().close();
        } catch (Exception e) {
            AlertUtil.error("Không thể kết nối cơ sở dữ liệu",
                    "Kiểm tra MySQL đang chạy và thông tin trong db.properties.\n" + e.getMessage());
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setTitle("Quản lý Bán hàng & Kho — Nhóm 6");
            stage.setScene(new Scene(root, 900, 550));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            AlertUtil.error("Lỗi khởi động", e.getMessage());
        }
    }

    @Override
    public void stop() {
        DBConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
