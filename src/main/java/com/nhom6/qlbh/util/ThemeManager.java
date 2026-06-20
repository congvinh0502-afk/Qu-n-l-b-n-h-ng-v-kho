package com.nhom6.qlbh.util;

import javafx.scene.Parent;

/**
 * Quản lý chủ đề Sáng/Tối toàn ứng dụng.
 * Gọi ThemeManager.applyTo(parent) sau mỗi lần load FXML.
 */
public class ThemeManager {

    private static boolean dark = false;

    private static final String LIGHT = "/css/style.css";
    private static final String DARK  = "/css/dark.css";

    public static boolean isDark() { return dark; }

    public static void toggle() { dark = !dark; }

    /**
     * Xóa stylesheet cũ của node và áp dụng theme hiện tại.
     * Light mode → chỉ style.css
     * Dark mode  → style.css + dark.css (overrides màu)
     */
    public static void applyTo(Parent node) {
        if (node == null) return;
        String lightUrl = ThemeManager.class.getResource(LIGHT).toExternalForm();
        node.getStylesheets().clear();
        node.getStylesheets().add(lightUrl);
        if (dark) {
            String darkUrl = ThemeManager.class.getResource(DARK).toExternalForm();
            node.getStylesheets().add(darkUrl);
        }
    }
}
