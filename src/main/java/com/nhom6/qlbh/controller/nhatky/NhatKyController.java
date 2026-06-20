package com.nhom6.qlbh.controller.nhatky;

import com.nhom6.qlbh.model.NhatKyHoatDong;
import com.nhom6.qlbh.service.NhatKyHoatDongService;
import com.nhom6.qlbh.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NhatKyController {

    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private ComboBox<String> cboNguoiDung;
    @FXML private TableView<NhatKyHoatDong> tblLog;
    @FXML private TableColumn<NhatKyHoatDong, String> colThoiGian;
    @FXML private TableColumn<NhatKyHoatDong, String> colNguoiDung;
    @FXML private TableColumn<NhatKyHoatDong, String> colBang;
    @FXML private TableColumn<NhatKyHoatDong, String> colHanhDong;
    @FXML private TableColumn<NhatKyHoatDong, String> colMaBanGhi;
    @FXML private TableColumn<NhatKyHoatDong, String> colMoTa;
    @FXML private Label lblCount;

    private final NhatKyHoatDongService service = new NhatKyHoatDongService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpTo.setValue(LocalDate.now());
        setupTable();
        loadNguoiDungs();
        loadData(null, null, null);
    }

    private void setupTable() {
        colThoiGian.setCellValueFactory(c -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "—");
        });
        colNguoiDung.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNguoiDung()));
        colBang.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBangTacDong()));
        colMaBanGhi.setCellValueFactory(c -> {
            String v = c.getValue().getMaBanGhi();
            return new SimpleStringProperty(v != null ? v : "—");
        });
        colMoTa.setCellValueFactory(c -> {
            String v = c.getValue().getMoTa();
            return new SimpleStringProperty(v != null ? v : "—");
        });

        colHanhDong.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHanhDongLabel()));
        colHanhDong.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle(switch (val) {
                    case "Thêm" -> "-fx-text-fill:#16a34a; -fx-font-weight:bold; -fx-alignment:CENTER;";
                    case "Sửa"  -> "-fx-text-fill:#e65100; -fx-font-weight:bold; -fx-alignment:CENTER;";
                    case "Xóa"  -> "-fx-text-fill:#c62828; -fx-font-weight:bold; -fx-alignment:CENTER;";
                    default     -> "-fx-alignment:CENTER;";
                });
            }
        });
    }

    private void loadNguoiDungs() {
        try {
            List<String> list = service.getNguoiDungs();
            list.add(0, "Tất cả");
            cboNguoiDung.setItems(FXCollections.observableArrayList(list));
            cboNguoiDung.getSelectionModel().selectFirst();
        } catch (Exception ignored) {}
    }

    @FXML
    private void onFilter() {
        LocalDate from = dpFrom.getValue();
        LocalDate to   = dpTo.getValue();
        String nd = cboNguoiDung.getValue();
        if ("Tất cả".equals(nd)) nd = null;
        loadData(from, to, nd);
    }

    @FXML
    private void onXemTatCa() {
        dpFrom.setValue(null);
        dpTo.setValue(null);
        cboNguoiDung.getSelectionModel().selectFirst();
        loadData(null, null, null);
    }

    private void loadData(LocalDate from, LocalDate to, String nguoiDung) {
        try {
            List<NhatKyHoatDong> list = service.findAll(from, to, nguoiDung);
            tblLog.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " bản ghi" + (list.size() == 500 ? " (giới hạn 500)" : ""));
        } catch (Exception e) {
            AlertUtil.error("Lỗi tải nhật ký", e.getMessage());
        }
    }
}
