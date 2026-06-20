package com.nhom6.qlbh.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final HikariDataSource ds;

    static {
        try (InputStream is = DBConnection.class.getResourceAsStream("/db.properties")) {
            Properties props = new Properties();
            props.load(is);

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(props.getProperty("db.url"));
            cfg.setUsername(props.getProperty("db.user"));
            cfg.setPassword(props.getProperty("db.password"));
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(30_000);
            cfg.setIdleTimeout(600_000);
            cfg.setMaxLifetime(1_800_000);

            ds = new HikariDataSource(cfg);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Không thể kết nối CSDL: " + e.getMessage());
        }
    }

    public static Connection get() throws SQLException {
        Connection conn = ds.getConnection();
        com.nhom6.qlbh.model.TaiKhoan user =
            com.nhom6.qlbh.service.AuthService.getCurrentUser();
        if (user != null) {
            try (PreparedStatement ps = conn.prepareStatement("SET @app_user = ?")) {
                ps.setString(1, user.getTenDangNhap());
                ps.executeUpdate();
            }
        }
        return conn;
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }
}
