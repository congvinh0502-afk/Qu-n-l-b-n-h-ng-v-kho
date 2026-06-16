package com.nhom6.qlbh.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertUtil {

    public static Optional<ButtonType> confirm(String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Xác nhận");
        a.setHeaderText(header);
        a.setContentText(content);
        return a.showAndWait();
    }

    public static void error(String header, String content) {
        show(Alert.AlertType.ERROR, header, content);
    }

    public static void info(String header, String content) {
        show(Alert.AlertType.INFORMATION, header, content);
    }

    public static void warn(String header, String content) {
        show(Alert.AlertType.WARNING, header, content);
    }

    private static void show(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle("Thông báo");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
