package com.nhom6.qlbh.controller.banhang;

import com.nhom6.qlbh.model.ChiTietHD;
import com.nhom6.qlbh.model.HoaDon;
import com.nhom6.qlbh.service.HoaDonService;
import com.nhom6.qlbh.util.AlertUtil;
import com.nhom6.qlbh.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonController {

    @FXML private ComboBox<String> cboFilter;
    @FXML private TableView<HoaDon> tblHD;
    @FXML private TableColumn<HoaDon, String> colMaHD, colKhachHang, colNhanVien, colThoiGian;
    @FXML private TableColumn<HoaDon, String> colTongTien, colGiamGia, colSauGiam, colDaTT, colConNo, colTrangThai;
    @FXML private TableColumn<HoaDon, Void> colAction;
    @FXML private Label lblCount;

    private final HoaDonService service = new HoaDonService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        cboFilter.setItems(FXCollections.observableArrayList("— Tất cả —", "CHUA_TT", "MOT_PHAN", "DA_TT"));
        cboFilter.getSelectionModel().selectFirst();

        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));
        colKhachHang.setCellValueFactory(new PropertyValueFactory<>("tenKH"));
        colNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNV"));
        colThoiGian.setCellValueFactory(c -> {
            var dt = c.getValue().getThoiGian();
            return new SimpleStringProperty(dt != null ? dt.format(FMT) : "");
        });
        colTongTien.setCellValueFactory(c -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getTongTienHang())));
        colGiamGia.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getGiamGia())));
        colSauGiam.setCellValueFactory(c  -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getTongSauGiamGia())));
        colDaTT.setCellValueFactory(c     -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getDaThanhToan())));
        colConNo.setCellValueFactory(c    -> new SimpleStringProperty(FormatUtil.currency(c.getValue().getConNo())));

        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThaiLabel()));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(switch (v) {
                    case "Đã TT"         -> "-fx-text-fill:#16a34a;-fx-font-weight:bold;-fx-alignment:CENTER;";
                    case "Trả một phần"  -> "-fx-text-fill:#ea580c;-fx-font-weight:bold;-fx-alignment:CENTER;";
                    default              -> "-fx-text-fill:#c62828;-fx-font-weight:bold;-fx-alignment:CENTER;";
                });
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnTT  = new Button("TT");
            private final Button btnTra = new Button("Trả");
            private final Button btnIn  = new Button("In");
            private final HBox box = new HBox(4, btnTT, btnTra, btnIn);
            {
                btnTT.getStyleClass().add("btn-primary");  btnTT.setPrefHeight(26);
                btnTra.getStyleClass().add("btn-edit");    btnTra.setPrefHeight(26);
                btnIn.getStyleClass().add("btn-search");   btnIn.setPrefHeight(26);
                btnTT.setOnAction(e  -> openThanhToan(getTableView().getItems().get(getIndex())));
                btnTra.setOnAction(e -> openTraHang(getTableView().getItems().get(getIndex())));
                btnIn.setOnAction(e  -> openIn(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HoaDon hd = getTableView().getItems().get(getIndex());
                btnTT.setDisable("DA_TT".equals(hd.getTrangThai()));
                setGraphic(box);
            }
        });

        loadData();
    }

    public void loadData() {
        try {
            String sel = cboFilter.getValue();
            String filter = (sel == null || sel.startsWith("—")) ? null : sel;
            List<HoaDon> list = service.findAll(filter);
            tblHD.setItems(FXCollections.observableArrayList(list));
            lblCount.setText(list.size() + " hóa đơn");
        } catch (Exception e) { AlertUtil.error("Lỗi tải dữ liệu", e.getMessage()); }
    }

    @FXML private void onSearch() { loadData(); }

    @FXML
    private void onTaoHD() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/banhang/hoa-don-form.fxml"));
            Parent form = loader.load();
            contentArea().getChildren().setAll(form);
        } catch (Exception e) { AlertUtil.error("Lỗi mở form", e.getMessage()); }
    }

    private void openThanhToan(HoaDon hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/banhang/thanh-toan-dialog.fxml"));
            Parent root = loader.load();
            ThanhToanDialogController ctrl = loader.getController();
            ctrl.setHoaDon(hd);
            Stage stage = new Stage();
            stage.setTitle("Thanh toán – " + hd.getMaHD());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            if (ctrl.isSaved()) loadData();
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }

    private void openTraHang(HoaDon hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/banhang/tra-hang-form.fxml"));
            Parent form = loader.load();
            TraHangFormController ctrl = loader.getController();
            ctrl.preloadHoaDon(hd.getMaHD());
            contentArea().getChildren().setAll(form);
        } catch (Exception e) { AlertUtil.error("Lỗi mở form trả hàng", e.getMessage()); }
    }

    private void openIn(HoaDon hd) {
        try {
            List<ChiTietHD> chiTiet = service.findChiTiet(hd.getMaHD());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/banhang/hoa-don-in.fxml"));
            Parent root = loader.load();
            HoaDonInController ctrl = loader.getController();
            ctrl.setData(hd, chiTiet);
            Stage stage = new Stage();
            stage.setTitle("In hóa đơn – " + hd.getMaHD());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) { AlertUtil.error("Lỗi", e.getMessage()); }
    }

    private StackPane contentArea() {
        return (StackPane) tblHD.getScene().getRoot().lookup("#contentArea");
    }
}
